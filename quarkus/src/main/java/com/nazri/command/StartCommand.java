package com.nazri.command;

import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import com.nazri.util.Constant;
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
        SendMessage response = new SendMessage(String.valueOf(message.getChatId()),
                getBody());
        response.setParseMode(Constant.MARKDOWN);

        if (userService.findOne(message.getChatId()) == null) {
            userService.create(message.getChat());
        }

        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public String getBody() {
        return "Welcome to *Currency Coinverter Bot* 🌎💱! \n" +
                "I’m here to make currency conversion simple and fast. \n\n" +
                "✨ *Getting Started:* \n" +
                "• Set your input and output currencies using `/from` and `/to` with currency codes or country names. \n\n" +
                "_Examples:_ \n" +
                "`/from MYR` 🇲🇾 or `/from Malaysia`. \n" +
                "_Alternatively, send your location 🌍 to automatically set your input currency based on where you are._\n\n"+
                "`/to SGD` 🇸🇬 or `/to Singapore` \n\n" +

                "• Send me the amount, and I’ll handle the rest 💡. \n\n" +
                "Let’s get started 🚀!";


    }
}
