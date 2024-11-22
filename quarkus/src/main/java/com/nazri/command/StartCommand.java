package com.nazri.command;

import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class StartCommand implements Command{

    @Inject
    TelegramBot telegramBot;

    @Inject
    UserService userService;

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public void execute(Message message, String args) {
        userService.create(message.getChat());
        SendMessage response = new SendMessage(String.valueOf(message.getChatId()),
                getBody());

        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public String getBody() {
        return "Welcome to the Currency Coinverter Bot! " +
                "I'm here to help you __coinvert__ currencies easily. " +
                "To get started, just use /to and /from with either currency codes or country names. For example, /to SGD or /to Singapore, and /from MYR or /from Malaysia. " +
                "Once you've set your currencies, just send me the amount, and I'll do the conversion for you! Let me know what you need, and I'll take care of the rest.";

    }
}
