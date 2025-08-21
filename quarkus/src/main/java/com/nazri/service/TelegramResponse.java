package com.nazri.service;

import com.nazri.util.Constant;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class TelegramResponse {
    private String text;
    private String parseMode = Constant.MARKDOWN;
    private InlineKeyboardMarkup keyboard;

    private TelegramResponse() {}

    public static TelegramResponse builder() {
        return new TelegramResponse();
    }

    public TelegramResponse text(String text) {
        this.text = text;
        return this;
    }

    public TelegramResponse parseMode(String parseMode) {
        this.parseMode = parseMode;
        return this;
    }

    public TelegramResponse keyboard(InlineKeyboardMarkup keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public SendMessage toMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(parseMode);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        return message;
    }

    public SendMessage toMessage(Long chatId) {
        return toMessage(String.valueOf(chatId));
    }
}
