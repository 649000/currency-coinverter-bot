package com.nazri.command;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface Command {
    String getName();

//    void execute(Message message, String args);

    default void handleCallback(CallbackQuery callbackQuery, String data) {
    }
}
