package com.nazri.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

public class Util {

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

    public static String getFlagEmoji(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            throw new IllegalArgumentException("Country code must be a 2-letter string.");
        }

        // Convert to uppercase to handle lowercase inputs
        countryCode = countryCode.toUpperCase();

        // Calculate the regional indicator symbols
        int firstChar = countryCode.charAt(0) - 'A' + 0x1F1E6;
        int secondChar = countryCode.charAt(1) - 'A' + 0x1F1E6;

        // Combine them into a string
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    public static String getCountryCodeFromCurrency(String currencyCode) {
        // Validate input
        if (currencyCode == null || currencyCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currency code. Must be a 3-letter ISO 4217 code.");
        }

        // Loop through all available locales to find the matching country
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                // Get the currency for this locale
                Currency currency = Currency.getInstance(locale);

                if (currency != null && currency.getCurrencyCode().equalsIgnoreCase(currencyCode)) {
                    // Return the country code (ISO 3166-1 alpha-2)
                    return locale.getCountry();
                }
            } catch (Exception ignored) {
                // Some locales might not have a currency
            }
        }

        return null;
    }

    public static String getFlagFromCurrencyCode(final String currencyCode) {
        final String countryCode = getCountryCodeFromCurrency(currencyCode);
        return getFlagEmoji(countryCode);
    }
}
