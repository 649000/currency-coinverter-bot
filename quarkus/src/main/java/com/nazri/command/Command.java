package com.nazri.command;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command {
    String getName();

    void execute(Message message, String args);

    default void handleCallback(CallbackQuery callbackQuery, String data) {
    }
}
