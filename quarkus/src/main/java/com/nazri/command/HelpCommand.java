package com.nazri.command;

import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.model.TelegramResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class HelpCommand implements Command {

    @Inject
    TelegramBot telegramBot;

    @Inject
    MessageService messageService;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(Message message, String args) {
        try {
            TelegramResponse response = messageService.createResponse("help.content");
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
