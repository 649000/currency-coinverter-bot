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
public class StartCommand implements Command {

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

                "🏁*How to Get Started:* \n\n" +

                "➖*Set your input currency*: Use `/from <currency code>` or `/from <country name>`.\n" +
                "  Example:\n" +
                "  `/from MYR`\n" +
                "  `/from Malaysia`\n\n" +
                "  Alternatively, send your location 🌍 to automatically set your input currency based on your current location.\n\n" +

                "➖*Set your output currency*: Use `/to <currency code>` or `/to <country name>`.\n" +
                "  Example:\n" +
                "  `/to SGD`\n" +
                "  `/to Singapore`\n\n" +

                "✨*Ready to Coinvert?*✨\n" +
                "Send me the amount, and I’ll handle the rest 💡. \n\n" +

                "Let’s get started 🚀!";
    }
}
