package com.nazri.command;

import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.TelegramResponse;
import com.nazri.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class StartCommand implements Command {

    @Inject
    TelegramBot telegramBot;

    @Inject
    UserService userService;

    @Inject
    MessageService messageService;

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public void execute(Message message, String args) {
        TelegramResponse response = messageService.createResponse("start.welcome");

        if (userService.findOne(message.getChatId()) == null) {
            userService.create(message.getChat());
        }

        try {
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
