package com.nazri.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashMap;
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

    public static HashMap<String, String> topInputCurrencies() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("THB", "🇹🇭");
        hashMap.put("MYR", "🇲🇾");
        hashMap.put("JPY", "🇯🇵");
        hashMap.put("IDR", "🇮🇩");
        hashMap.put("KRW", "🇰🇷");
        return hashMap;
    }

    public static HashMap<String, String> topOutputCurrencies() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("SGD", "🇸🇬");
        hashMap.put("USD", "🇺🇸");
        hashMap.put("JPY", "🇯🇵");
        hashMap.put("EUR", "🇪🇺");
        hashMap.put("GBP", "🇬🇧");
        return hashMap;
    }

    /**
     * Formats a given number as a nicely formatted money string.
     *
     * @param amount   The monetary value to format.
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
    private static Locale findLocaleForCurrency(Currency currency) {
        // Get all available locales
        Locale[] allLocales = Locale.getAvailableLocales();

        // First try: Find a locale where this is the primary currency
        for (Locale locale : allLocales) {
            try {
                if (Currency.getInstance(locale).getCurrencyCode()
                        .equals(currency.getCurrencyCode())) {
                    return locale;
                }
            } catch (IllegalArgumentException e) {
                // Skip locales without currency information
                continue;
            }
        }

        // Fallback to US locale if no matching locale found
        return Locale.US;
    }
}
