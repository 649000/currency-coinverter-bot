package com.nazri.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating Telegram inline keyboards with various button arrangements.
 * 
 * This class provides methods to generate keyboard layouts for currency selection,
 * amount selection, and multiplier operations in a Telegram bot interface.
 */
@ApplicationScoped
public class KeyboardUtil {
    
    private static final int MAX_BUTTONS_PER_ROW = 3;
    
    /**
     * Creates an inline keyboard markup for currency selection.
     * 
     * Each button displays the currency's emoji flag and code, with callback data
     * formatted as "{commandPrefix}:{currencyCode}".
     * 
     * @param currencies List of currency codes to create buttons for
     * @param commandPrefix Prefix for callback data commands
     * @return InlineKeyboardMarkup with currency selection buttons
     */
    public static InlineKeyboardMarkup createCurrencyKeyboard(List<String> currencies, String commandPrefix) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String currencyCode : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(Util.getEmojiFlag(currencyCode) + " " + currencyCode.toUpperCase());
            button.setCallbackData(commandPrefix + ":" + currencyCode);
            rowInline.add(button);
        }

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    /**
     * Creates an inline keyboard markup for amount selection.
     * 
     * Each button displays the amount value, with callback data formatted as
     * "{commandPrefix}:{amount}".
     * 
     * @param amounts List of amount values to create buttons for
     * @param commandPrefix Prefix for callback data commands
     * @return InlineKeyboardMarkup with amount selection buttons
     */
    public static InlineKeyboardMarkup createAmountKeyboard(List<String> amounts, String commandPrefix) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String amount : amounts) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(amount);
            button.setCallbackData(commandPrefix + ":" + amount);
            rowInline.add(button);
        }

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    /**
     * Creates an inline keyboard markup for multiplier operations.
     * 
     * Buttons are arranged in rows with a maximum of 3 buttons per row.
     * Each button displays a multiplier symbol and calculates the resulting value
     * by multiplying the input amount with the multiplier. Callback data is
     * formatted as "{commandPrefix}:{calculatedValue}".
     * 
     * @param inputAmount The base amount to multiply
     * @param multiplierList List of multiplier values as strings
     * @param multiplierSymbols List of symbols to display on buttons
     * @param commandPrefix Prefix for callback data commands
     * @return InlineKeyboardMarkup with multiplier operation buttons
     * @throws IllegalArgumentException if input parameters are invalid or lists have mismatched sizes
     */
    public static InlineKeyboardMarkup createMultiplierKeyboard(BigDecimal inputAmount, List<String> multiplierList, 
                                                               List<String> multiplierSymbols, String commandPrefix) {
        if (inputAmount == null || multiplierList == null || multiplierList.isEmpty() || 
            multiplierSymbols == null || multiplierList.size() != multiplierSymbols.size()) {
            return new InlineKeyboardMarkup();
        }
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 0; i < multiplierList.size(); i++) {
            if (i > 0 && i % MAX_BUTTONS_PER_ROW == 0) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
            
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(multiplierSymbols.get(i));
            BigDecimal multiplier = new BigDecimal(multiplierList.get(i));
            BigDecimal value = inputAmount.multiply(multiplier);
            button.setCallbackData(commandPrefix + ":" + value);
            rowInline.add(button);
        }

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
