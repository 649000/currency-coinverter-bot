package com.nazri.service;

import com.nazri.client.CurrencyApiClient;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@ApplicationScoped
public class CurrencyService {

    private static final Logger log = Logger.getLogger(CurrencyService.class);

    @ConfigProperty(name = "currency.list.api.url")
    String currencyListapiUrl;

    @Inject
    @RestClient
    CurrencyApiClient currencyApiClient;

    // In-memory cache for exchange rates - survives across Lambda invocations in same container
    private static final Map<String, CachedRate> RATE_CACHE = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    
    // Circuit breaker state for external API resilience
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static final AtomicLong lastFailureTime = new AtomicLong(0);
    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration CIRCUIT_TIMEOUT = Duration.ofMinutes(5);
    
    // Configuration constants
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
    private static final int MAX_TARGET_CURRENCIES = 10;
    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 1000;

    /**
     * Cached exchange rate entry with timestamp for TTL management
     */
    private static class CachedRate {
        final Map<String, BigDecimal> rates;
        final Instant timestamp;
        
        CachedRate(Map<String, BigDecimal> rates) {
            this.rates = new HashMap<>(rates);
            this.timestamp = Instant.now();
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(timestamp.plus(CACHE_TTL));
        }
    }

    /**
     * Converts an amount from one currency to multiple target currencies.
     * Implements caching, circuit breaker, and retry logic for Lambda optimization.
     *
     * @param amount The amount to convert (must be positive and <= 1,000,000)
     * @param fromCurrency Source currency code or country name
     * @param toCurrencies List of target currency codes (max 10)
     * @return Map of currency codes to converted amounts
     * @throws WebApplicationException if validation fails or service is unavailable
     */
    public Map<String, BigDecimal> convertCurrency(BigDecimal amount, String fromCurrency, List<String> toCurrencies) {
        validateInputs(amount, fromCurrency, toCurrencies);
        
        // Check circuit breaker before attempting external calls
        if (isCircuitOpen()) {
            log.warn("Circuit breaker is open, rejecting request");
            throw new WebApplicationException(
                "Currency service temporarily unavailable", 
                Response.Status.SERVICE_UNAVAILABLE
            );
        }

        try {
            Map<String, BigDecimal> exchangeRates = fetchExchangeRates(fromCurrency, toCurrencies);
            Map<String, BigDecimal> result = performConversions(amount, exchangeRates);
            recordSuccess();
            return result;
        } catch (WebApplicationException e) {
            recordFailure();
            // Re-throw WebApplicationException as-is to preserve status codes
            throw e;
        } catch (Exception e) {
            recordFailure();
            log.errorf("Currency conversion failed: %s", e.getMessage());
            throw new WebApplicationException(
                "Currency conversion failed", 
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Validates input parameters for currency conversion.
     * Ensures amount is within limits and currencies are valid.
     */
    private void validateInputs(BigDecimal amount, String fromCurrency, List<String> toCurrencies) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warnf("Invalid amount: %s", amount);
            throw new WebApplicationException(
                    "Amount must be positive",
                    Response.Status.BAD_REQUEST
            );
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            log.warnf("Amount too large: %s", amount);
            throw new WebApplicationException(
                    "Amount too large (max 1,000,000)",
                    Response.Status.BAD_REQUEST
            );
        }

        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            log.warn("Empty source currency");
            throw new WebApplicationException(
                    "Source currency required",
                    Response.Status.BAD_REQUEST
            );
        }

        if (toCurrencies == null || toCurrencies.isEmpty()) {
            log.warn("No target currencies provided");
            throw new WebApplicationException(
                    "Target currencies required",
                    Response.Status.BAD_REQUEST
            );
        }
        
        if (toCurrencies.size() > MAX_TARGET_CURRENCIES) {
            log.warnf("Too many target currencies: %d", toCurrencies.size());
            throw new WebApplicationException(
                    "Too many target currencies (max 10)",
                    Response.Status.BAD_REQUEST
            );
        }
        
        // Validate source currency code
        String normalizedFrom = getCurrencyCode(fromCurrency);
        if (normalizedFrom == null) {
            log.warnf("Invalid source currency: %s", fromCurrency);
            throw new WebApplicationException(
                    "Invalid source currency: " + fromCurrency,
                    Response.Status.BAD_REQUEST
            );
        }
    }

    /**
     * Fetches exchange rates with caching using Quarkus REST Client.
     * Fault tolerance (retry, circuit breaker, timeout) is handled by REST client annotations.
     * Cache key includes sorted target currencies for optimal hit rate.
     */
    private Map<String, BigDecimal> fetchExchangeRates(String fromCurrency, List<String> toCurrencies) {
        
        // Create cache key with sorted currencies for better hit rate
        String cacheKey = fromCurrency.toUpperCase() + ":" + 
                         toCurrencies.stream()
                                   .map(String::toUpperCase)
                                   .sorted()
                                   .collect(Collectors.joining(","));
        
        // Check cache first - significant performance boost for Lambda
        CachedRate cached = RATE_CACHE.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.infof("Cache hit for %s", cacheKey);
            return filterRequestedCurrencies(cached.rates, toCurrencies);
        }
        
        log.infof("Cache miss for %s, fetching from API", cacheKey);
        
        try {
            // Use REST Client - fault tolerance is handled by annotations
            Map<String, Object> response = currencyApiClient.getExchangeRates(fromCurrency.toLowerCase());
            Map<String, BigDecimal> rates = parseExchangeRates(response, fromCurrency, toCurrencies);
            
            // Cache the result
            RATE_CACHE.put(cacheKey, new CachedRate(rates));
            
            // Periodic cache cleanup to prevent memory issues in long-running containers
            cleanExpiredCache();
            
            return rates;
        } catch (Exception e) {
            log.errorf("Failed to fetch exchange rates: %s", e.getMessage());
            throw new WebApplicationException(
                "Failed to fetch exchange rates", 
                Response.Status.SERVICE_UNAVAILABLE
            );
        }
    }

    /**
     * Parses REST client response and extracts requested exchange rates.
     */
    private Map<String, BigDecimal> parseExchangeRates(Map<String, Object> response, String fromCurrency, List<String> toCurrencies) {
        Map<String, BigDecimal> rates = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> ratesData = (Map<String, Object>) response.get(fromCurrency.toLowerCase());
            
            if (ratesData == null) {
                throw new WebApplicationException("No rates found for currency: " + fromCurrency, Response.Status.NOT_FOUND);
            }

            for (String toCurrency : toCurrencies) {
                String normalizedCurrency = toCurrency.toLowerCase();
                Object rateValue = ratesData.get(normalizedCurrency);
                
                if (rateValue != null) {
                    BigDecimal rate = new BigDecimal(rateValue.toString());
                    rates.put(toCurrency.toUpperCase(), rate);
                } else {
                    log.warnf("No rate found for currency: %s", toCurrency);
                }
            }
        } catch (Exception e) {
            log.errorf("Failed to parse API response: %s", e.getMessage());
            throw new WebApplicationException(
                    "Invalid API response format",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }

        if (rates.isEmpty()) {
            log.warn("No exchange rates found in API response");
            throw new WebApplicationException(
                    "No exchange rates found",
                    Response.Status.NOT_FOUND
            );
        }

        return rates;
    }

    /**
     * Performs currency conversions using fetched exchange rates.
     * Applies consistent rounding for financial calculations.
     */
    private Map<String, BigDecimal> performConversions(BigDecimal amount, Map<String, BigDecimal> exchangeRates) {
        Map<String, BigDecimal> conversions = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : exchangeRates.entrySet()) {
            BigDecimal convertedAmount = amount.multiply(entry.getValue())
                    .setScale(2, RoundingMode.HALF_UP);
            conversions.put(entry.getKey(), convertedAmount);
        }
        return conversions;
    }

    /**
     * Converts various input formats (currency code, country code, country name) 
     * to standardized currency code.
     */
    public String getCurrencyCode(String input) {
        if (input == null) return null;

        String normalized = input.trim().toUpperCase();

        // Try direct currency code match (e.g., "USD", "EUR")
        try {
            return Currency.getInstance(normalized).getCurrencyCode();
        } catch (Exception ignored) {
            // Not a valid currency code, continue
        }

        // Try country code match (e.g., "US" -> "USD", "MY" -> "MYR")
        try {
            Locale countryLocale = new Locale("", normalized);
            return Currency.getInstance(countryLocale).getCurrencyCode();
        } catch (Exception ignored) {
            // Not a valid country code, continue
        }

        // Try full country name match (e.g., "Malaysia" -> "MYR")
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry(Locale.ENGLISH).toUpperCase().equals(normalized)) {
                try {
                    return Currency.getInstance(locale).getCurrencyCode();
                } catch (Exception ignored) {
                    // Continue searching
                }
            }
        }

        return null;
    }

    // ==================== Circuit Breaker Implementation ====================
    
    /**
     * Checks if circuit breaker is open based on failure count and timeout.
     */
    private boolean isCircuitOpen() {
        if (failureCount.get() < FAILURE_THRESHOLD) {
            return false;
        }
        
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        if (timeSinceLastFailure > CIRCUIT_TIMEOUT.toMillis()) {
            // Reset circuit breaker after timeout
            log.info("Circuit breaker timeout expired, resetting");
            failureCount.set(0);
            return false;
        }
        
        return true;
    }
    
    /**
     * Records a failure for circuit breaker tracking.
     */
    private void recordFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        log.warnf("Recorded failure #%d", failures);
    }
    
    /**
     * Records a success and resets circuit breaker if needed.
     */
    private void recordSuccess() {
        if (failureCount.get() > 0) {
            log.info("Service recovered, resetting circuit breaker");
            failureCount.set(0);
        }
    }

    // ==================== Cache Management ====================
    
    /**
     * Filters cached rates to return only requested currencies.
     * Handles cases where cache contains more currencies than requested.
     */
    private Map<String, BigDecimal> filterRequestedCurrencies(Map<String, BigDecimal> allRates, List<String> requested) {
        return requested.stream()
                .map(String::toUpperCase)
                .filter(allRates::containsKey)
                .collect(Collectors.toMap(
                    currency -> currency,
                    allRates::get
                ));
    }
    
    /**
     * Removes expired entries from cache to prevent memory leaks.
     * Only runs when cache size exceeds threshold to minimize overhead.
     */
    private void cleanExpiredCache() {
        if (RATE_CACHE.size() > MAX_CACHE_SIZE) {
            int sizeBefore = RATE_CACHE.size();
            RATE_CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
            int sizeAfter = RATE_CACHE.size();
            log.infof("Cache cleanup: removed %d expired entries (%d -> %d)", 
                     sizeBefore - sizeAfter, sizeBefore, sizeAfter);
        }
    }

    /**
     * Cleanup method called when application shuts down.
     * Important for proper resource management in Lambda.
     */
    @PreDestroy
    void cleanup() {
        RATE_CACHE.clear();
        log.info("Currency service cache cleared on shutdown");
    }

    // TODO: Future enhancement - implement currency list fetching
    // public void fetchCurrencies() throws IOException, InterruptedException { ... }
}
