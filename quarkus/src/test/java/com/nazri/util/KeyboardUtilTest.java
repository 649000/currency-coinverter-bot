package com.nazri.util;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardUtilTest {

    @Test
    void createCurrencyKeyboard_ShouldCreateSingleRowWithFormattedButtons() {
        List<String> currencies = Arrays.asList("USD", "EUR", "SGD");
        String prefix = "from";

        InlineKeyboardMarkup markup = KeyboardUtil.createCurrencyKeyboard(currencies, prefix);

        assertNotNull(markup);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());

        List<InlineKeyboardButton> row = keyboard.get(0);
        assertEquals(3, row.size());

        assertEquals(Util.getEmojiFlag("USD") + " USD", row.get(0).getText());
        assertEquals("from:USD", row.get(0).getCallbackData());

        assertEquals(Util.getEmojiFlag("EUR") + " EUR", row.get(1).getText());
        assertEquals("from:EUR", row.get(1).getCallbackData());

        assertEquals(Util.getEmojiFlag("SGD") + " SGD", row.get(2).getText());
        assertEquals("from:SGD", row.get(2).getCallbackData());
    }

    @Test
    void createCurrencyKeyboard_ShouldHandleEmptyList() {
        InlineKeyboardMarkup markup = KeyboardUtil.createCurrencyKeyboard(Collections.emptyList(), "from");

        assertNotNull(markup);
        assertEquals(1, markup.getKeyboard().size());
        assertTrue(markup.getKeyboard().get(0).isEmpty());
    }

    @Test
    void createAmountKeyboard_ShouldCreateButtonsWithExactAmounts() {
        List<String> amounts = Arrays.asList("10", "50", "100", "500");
        String prefix = "convert";

        InlineKeyboardMarkup markup = KeyboardUtil.createAmountKeyboard(amounts, prefix);

        assertNotNull(markup);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());

        List<InlineKeyboardButton> row = keyboard.get(0);
        assertEquals(4, row.size());

        for (int i = 0; i < amounts.size(); i++) {
            assertEquals(amounts.get(i), row.get(i).getText());
            assertEquals(prefix + ":" + amounts.get(i), row.get(i).getCallbackData());
        }
    }

    @Test
    void createAmountKeyboard_ShouldHandleEmptyList() {
        InlineKeyboardMarkup markup = KeyboardUtil.createAmountKeyboard(Collections.emptyList(), "convert");

        assertNotNull(markup);
        assertEquals(1, markup.getKeyboard().size());
        assertTrue(markup.getKeyboard().get(0).isEmpty());
    }

    @Test
    void createMultiplierKeyboard_ShouldCalculateMultipliedValuesCorrectly() {
        BigDecimal inputAmount = new BigDecimal("25");
        List<String> multipliers = Arrays.asList("1", "2", "10");
        List<String> symbols = Arrays.asList("1x", "2x", "10x");
        String prefix = "convert";

        InlineKeyboardMarkup markup = KeyboardUtil.createMultiplierKeyboard(inputAmount, multipliers, symbols, prefix);

        assertNotNull(markup);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());

        List<InlineKeyboardButton> row = keyboard.get(0);
        assertEquals(3, row.size());

        assertEquals("1x", row.get(0).getText());
        assertEquals("convert:25", row.get(0).getCallbackData());

        assertEquals("2x", row.get(1).getText());
        assertEquals("convert:50", row.get(1).getCallbackData());

        assertEquals("10x", row.get(2).getText());
        assertEquals("convert:250", row.get(2).getCallbackData());
    }

    @Test
    void createMultiplierKeyboard_ShouldSplitIntoMultipleRows_WhenExceedingMaxButtonsPerRow() {
        BigDecimal inputAmount = new BigDecimal("10");
        List<String> multipliers = Arrays.asList("1", "2", "3", "4", "5");
        List<String> symbols = Arrays.asList("1x", "2x", "3x", "4x", "5x");
        String prefix = "convert";

        InlineKeyboardMarkup markup = KeyboardUtil.createMultiplierKeyboard(inputAmount, multipliers, symbols, prefix);

        assertNotNull(markup);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(2, keyboard.size(), "Should wrap into two rows (3 in first, 2 in second)");

        assertEquals(3, keyboard.get(0).size());
        assertEquals(2, keyboard.get(1).size());

        assertEquals("convert:40", keyboard.get(1).get(0).getCallbackData());
        assertEquals("convert:50", keyboard.get(1).get(1).getCallbackData());
    }

    @Test
    void createMultiplierKeyboard_ShouldReturnEmptyMarkup_WhenInputsAreInvalid() {
        BigDecimal amount = new BigDecimal("10");
        List<String> validList = Arrays.asList("1", "2");

        // Null amount
        InlineKeyboardMarkup m1 = KeyboardUtil.createMultiplierKeyboard(null, validList, validList, "convert");
        assertNull(m1.getKeyboard());

        // Null multipliers
        InlineKeyboardMarkup m2 = KeyboardUtil.createMultiplierKeyboard(amount, null, validList, "convert");
        assertNull(m2.getKeyboard());

        // Empty multipliers
        InlineKeyboardMarkup m3 = KeyboardUtil.createMultiplierKeyboard(amount, Collections.emptyList(), Collections.emptyList(), "convert");
        assertNull(m3.getKeyboard());

        // Null symbols
        InlineKeyboardMarkup m4 = KeyboardUtil.createMultiplierKeyboard(amount, validList, null, "convert");
        assertNull(m4.getKeyboard());

        // Mismatched list sizes
        InlineKeyboardMarkup m5 = KeyboardUtil.createMultiplierKeyboard(amount, validList, Collections.singletonList("1x"), "convert");
        assertNull(m5.getKeyboard());
    }
}
