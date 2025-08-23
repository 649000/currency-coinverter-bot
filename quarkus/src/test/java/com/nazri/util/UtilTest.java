package com.nazri.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testGetCurrentTime() {
        String currentTime = Util.getCurrentTime();
        assertNotNull(currentTime);
        assertFalse(currentTime.isEmpty());
        assertTrue(currentTime.contains("T")); // ISO format contains T
        assertTrue(currentTime.contains("+") || currentTime.contains("Z")); // Timezone has + offset or Z
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
        // The formatted result should contain the numeric value
        assertTrue(result.contains("1234.56") || result.contains("1,234.56") || result.contains("1.234,56"));
    }

    @Test
    void testFormatMoneyWithInvalidCurrency() {
        BigDecimal amount = new BigDecimal("1234.56");
        assertThrows(IllegalArgumentException.class, () -> {
            Util.formatMoney(amount, "INVALID");
        });
    }

    @Test
    void testFormatMoneyWithDifferentCurrencies() {
        BigDecimal amount = new BigDecimal("1000.00");

        // Test EUR
        String eurResult = Util.formatMoney(amount, "EUR");
        assertNotNull(eurResult);
        assertTrue(eurResult.contains("EUR"));

        // Test JPY (no decimal places)
        String jpyResult = Util.formatMoney(amount, "JPY");
        assertNotNull(jpyResult);
        assertTrue(jpyResult.contains("JPY"));
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
