package com.nazri.model;

import com.nazri.util.Constant;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TelegramResponseTest {

    @Test
    void builder_ShouldCreateMessageWithDefaultMarkdownParseMode() {
        TelegramResponse response = TelegramResponse.builder()
                .text("Hello World");

        SendMessage message = response.toMessage(12345L);

        assertEquals("12345", message.getChatId());
        assertEquals("Hello World", message.getText());
        assertEquals(Constant.MARKDOWN, message.getParseMode());
        assertNull(message.getReplyMarkup());
    }

    @Test
    void builder_ShouldSetCustomParseModeAndKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Click me");
        button.setCallbackData("test:data");
        keyboard.setKeyboard(Collections.singletonList(Collections.singletonList(button)));

        TelegramResponse response = TelegramResponse.builder()
                .text("Custom message")
                .parseMode("HTML")
                .keyboard(keyboard);

        SendMessage message = response.toMessage("98765");

        assertEquals("98765", message.getChatId());
        assertEquals("Custom message", message.getText());
        assertEquals("HTML", message.getParseMode());
        assertNotNull(message.getReplyMarkup());
        assertEquals(keyboard, message.getReplyMarkup());
    }
}
