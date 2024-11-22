package com.nazri.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CurrencyService {

    private static final Logger log = Logger.getLogger(CurrencyService.class);

    @ConfigProperty(name = "currency.api.url")
    String apiUrl;

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public Map<String, BigDecimal> convertCurrency(BigDecimal amount, String fromCurrency, Set<String> toCurrencies) {
        validateInputs(amount, fromCurrency, toCurrencies);

        try {
            Map<String, BigDecimal> exchangeRates = fetchExchangeRates(fromCurrency, toCurrencies);
            return performConversions(amount, exchangeRates);
        } catch (Exception e) {
            log.warnf("Conversion error", e);
            throw new WebApplicationException(
                    "Currency conversion failed",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void validateInputs(BigDecimal amount, String fromCurrency, Set<String> toCurrencies) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warnf("Invalid amount: " + amount);
//            throw new WebApplicationException(
//                    "Amount must be positive",
//                    Response.Status.BAD_REQUEST
//            );
        }

        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            log.warnf("Empty source currency");
//            throw new WebApplicationException(
//                    "Source currency required",
//                    Response.Status.BAD_REQUEST
//            );
        }

        if (toCurrencies == null || toCurrencies.isEmpty()) {
            log.warnf("No target currencies");
//            throw new WebApplicationException(
//                    "Target currencies required",
//                    Response.Status.BAD_REQUEST
//            );
        }
    }

    private Map<String, BigDecimal> fetchExchangeRates(String fromCurrency, Set<String> toCurrencies) throws IOException, InterruptedException {
        String url = String.format(apiUrl, fromCurrency.toLowerCase());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new WebApplicationException(
                    "Failed to fetch exchange rates",
                    Response.Status.SERVICE_UNAVAILABLE
            );
        }

        return parseExchangeRates(response.body(), fromCurrency, toCurrencies);
    }

    private Map<String, BigDecimal> parseExchangeRates(String responseBody, String fromCurrency, Set<String> toCurrencies) {
        Map<String, BigDecimal> rates = new HashMap<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
            JsonObject ratesJson = jsonReader.readObject().getJsonObject(fromCurrency.toLowerCase());

            for (String toCurrency : toCurrencies) {
                String normalizedCurrency = toCurrency.toLowerCase();
                if (ratesJson.containsKey(normalizedCurrency)) {
                    BigDecimal rate = new BigDecimal(ratesJson.get(normalizedCurrency).toString());
                    rates.put(toCurrency.toUpperCase(), rate);
                } else {
                    log.warnf("No rate found for currency: " + toCurrency);
                }
            }
        }

        if (rates.isEmpty()) {
            throw new WebApplicationException(
                    "No exchange rates found",
                    Response.Status.NOT_FOUND
            );
        }

        return rates;
    }

    private Map<String, BigDecimal> performConversions(BigDecimal amount, Map<String, BigDecimal> exchangeRates) {
        Map<String, BigDecimal> conversions = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : exchangeRates.entrySet()) {
            conversions.put(
                    entry.getKey(),
                    amount.multiply(entry.getValue())
                            .setScale(2, RoundingMode.HALF_UP)
            );
        }
        return conversions;
    }

    public String getCurrencyCode(String input) {
        if (input == null) return null;

        String normalized = input.trim().toUpperCase();

        // Try direct currency code match
        try {
            return Currency.getInstance(normalized).getCurrencyCode();
        } catch (Exception ignored) {
        }

        // Try country code match
        try {
            Locale countryLocale = new Locale("", normalized);
            return Currency.getInstance(countryLocale).getCurrencyCode();
        } catch (Exception ignored) {
        }

        // Try full country name match
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry(Locale.ENGLISH).toUpperCase().equals(normalized)) {
                try {
                    return Currency.getInstance(locale).getCurrencyCode();
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    public Set<String> getAllCurrencyCodes() {
        return Currency.getAvailableCurrencies().stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());
    }
}
