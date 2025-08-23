package com.nazri.util;

import io.quarkus.cache.CacheResult;
import org.eclipse.microprofile.config.ConfigProvider;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class providing helper methods for currency conversion operations.
 * 
 * This class offers methods for formatting money values, validating numeric input,
 * and finding appropriate locales for currency formatting. It also provides
 * functionality for retrieving emoji flags for currency codes.
 */
public class Util {

    /**
     * Gets the current time in Singapore timezone formatted as ISO offset date time.
     * 
     * @return Current time as ISO formatted string (e.g., "2023-12-01T10:30:45+08:00")
     */
    public static String getCurrentTime() {
        return Instant.now().atZone(ZoneId.of("Asia/Singapore")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Checks if the input string is a valid numeric value, including decimals.
     *
     * @param input the string to check
     * @return true if the string is a valid numeric value, false otherwise
     */
    public static boolean isNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formats a given number as a nicely formatted money string.
     *
     * @param amount       The monetary value to format.
     * @param currencyCode The ISO 4217 currency code (e.g., "SGD", "USD").
     * @return A formatted money string.
     * @throws IllegalArgumentException if currency code is invalid
     */
    public static String formatMoney(BigDecimal amount, String currencyCode) {
        // Validate currency code
        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }

        // Find the most appropriate locale for this currency
        Locale locale = findLocaleForCurrency(currency);

        // Create formatter with the determined locale
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);

        // Format the amount
        String formatted = formatter.format(amount).trim();

        // Replace the currency symbol with the currency code
        return formatted.replace(currency.getSymbol(locale), currencyCode + " ");
    }

    /**
     * Finds the most appropriate locale for a given currency.
     * First tries to find a locale where this is the primary currency,
     * falls back to US locale if none found.
     *
     * @param currency The Currency to find a locale for
     * @return The most appropriate Locale
     */
    @CacheResult(cacheName = "currency-locales")
    private static Locale findLocaleForCurrency(Currency currency) {
        Locale[] allLocales = Locale.getAvailableLocales();
        for (Locale locale : allLocales) {
            try {
                if (Currency.getInstance(locale).getCurrencyCode().equals(currency.getCurrencyCode())) {
                    return locale;
                }
            } catch (IllegalArgumentException e) {
                // Skip locales without currency information
                continue;
            }
        }
        return Locale.US;
    }

    /**
     * Retrieves the emoji flag for a given currency code from configuration.
     * 
     * Looks up the emoji flag using the configuration property "currency.{currencyCode}"
     * (case insensitive). Returns a default money bag emoji "💰" if no configuration
     * is found for the currency code.
     * 
     * @param currencyCode The currency code to get emoji flag for (e.g., "USD", "EUR")
     * @return The emoji flag for the currency code or default money bag emoji
     */
    public static String getEmojiFlag(String currencyCode) {
        String propertyName = "currency." + currencyCode.toLowerCase();
        return ConfigProvider.getConfig()
                .getOptionalValue(propertyName, String.class)
                .orElse("💰");
    }
}
