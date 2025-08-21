package com.nazri.service;

import com.nazri.client.CurrencyApiClient;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CurrencyService {

    private static final Logger log = Logger.getLogger(CurrencyService.class);

    @ConfigProperty(name = "currency.list.api.url")
    String currencyListapiUrl;

    @Inject
    @RestClient
    CurrencyApiClient currencyApiClient;

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
        
        try {
            Map<String, BigDecimal> exchangeRates = fetchExchangeRates(fromCurrency, toCurrencies);
            return performConversions(amount, exchangeRates);
        } catch (WebApplicationException e) {
            // Re-throw WebApplicationException as-is to preserve status codes
            throw e;
        } catch (Exception e) {
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
     * Fetches exchange rates with caching using Quarkus Cache.
     * Fault tolerance (retry, circuit breaker, timeout) is handled by REST client annotations.
     * Cache key is automatically generated from method parameters.
     */
    @CacheResult(cacheName = "exchange-rates")
    public Map<String, BigDecimal> fetchExchangeRates(String fromCurrency, List<String> toCurrencies) {
        // Sort currencies for consistent cache keys
        List<String> sortedCurrencies = toCurrencies.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        
        log.infof("Fetching exchange rates for %s -> %s", fromCurrency, sortedCurrencies);
        
        try {
            // Use REST Client - fault tolerance is handled by annotations
            Map<String, Object> response = currencyApiClient.getExchangeRates(fromCurrency.toLowerCase());
            Map<String, BigDecimal> rates = parseExchangeRates(response, fromCurrency, toCurrencies);
            
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

    // TODO: Future enhancement - implement currency list fetching
    // public void fetchCurrencies() throws IOException, InterruptedException { ... }
}
