package com.nazri.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testGetCurrentTime() {
        String currentTime = Util.getCurrentTime();
        assertNotNull(currentTime);
        assertFalse(currentTime.isEmpty());
        assertTrue(currentTime.contains("T")); // ISO format contains T
        assertTrue(currentTime.contains("+")); // Singapore timezone has + offset
    }

    @Test
    void testIsNumericWithValidNumbers() {
        assertTrue(Util.isNumeric("123"));
        assertTrue(Util.isNumeric("123.45"));
        assertTrue(Util.isNumeric("0"));
        assertTrue(Util.isNumeric("-123.45"));
        assertTrue(Util.isNumeric("1.23E4"));
    }

    @Test
    void testIsNumericWithInvalidNumbers() {
        assertFalse(Util.isNumeric(null));
        assertFalse(Util.isNumeric(""));
        assertFalse(Util.isNumeric("abc"));
        assertFalse(Util.isNumeric("123abc"));
        assertFalse(Util.isNumeric("12.34.56"));
    }

    @Test
    void testFormatMoneyWithValidCurrency() {
        BigDecimal amount = new BigDecimal("1234.56");
        String result = Util.formatMoney(amount, "USD");
        assertNotNull(result);
        assertTrue(result.contains("USD"));
        assertTrue(result.contains("1,234.56") || result.contains("1.234,56")); // Format varies by locale
    }

    @Test
    void testFormatMoneyWithInvalidCurrency() {
        BigDecimal amount = new BigDecimal("1234.56");
        assertThrows(IllegalArgumentException.class, () -> {
            Util.formatMoney(amount, "INVALID");
        });
    }

    @Test
    void testFindLocaleForCurrency() {
        // Test that the method returns a valid locale
        Currency usd = Currency.getInstance("USD");
        Locale locale = Util.findLocaleForCurrency(usd);
        assertNotNull(locale);
        
        // Test with EUR
        Currency eur = Currency.getInstance("EUR");
        Locale eurLocale = Util.findLocaleForCurrency(eur);
        assertNotNull(eurLocale);
    }

    @Test
    void testGetEmojiFlagWithConfiguredCurrency() {
        // Test with USD which should have a flag configured
        String flag = Util.getEmojiFlag("USD");
        assertNotNull(flag);
        assertFalse(flag.isEmpty());
    }

    @Test
    void testGetEmojiFlagWithUnconfiguredCurrency() {
        // Test with a currency that likely doesn't have a specific flag configured
        String flag = Util.getEmojiFlag("XYZ");
        assertEquals("💰", flag); // Should return default money bag emoji
    }

    @Test
    void testGetEmojiFlagCaseInsensitive() {
        String flagUpper = Util.getEmojiFlag("USD");
        String flagLower = Util.getEmojiFlag("usd");
        assertEquals(flagUpper, flagLower);
    }
}
