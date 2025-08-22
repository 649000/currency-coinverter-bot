package com.nazri.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class KeyboardUtil {
    
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

    public static InlineKeyboardMarkup createMultiplierKeyboard(BigDecimal inputAmount, List<String> multiplierList, 
                                                               List<String> multiplierSymbols, String commandPrefix) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 0; i < multiplierList.size(); i++) {
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
