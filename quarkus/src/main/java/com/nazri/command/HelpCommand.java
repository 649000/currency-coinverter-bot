package com.nazri.command;

import com.nazri.service.TelegramBot;
import com.nazri.util.Constant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class HelpCommand implements Command {

    @Inject
    TelegramBot telegramBot;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(Message message, String args) {
        SendMessage response = new SendMessage(String.valueOf(message.getChatId()),
                getBody());
        response.setParseMode(Constant.MARKDOWN);

        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBody() {
        return "*Currency Coinverter Bot Help* 🌍💱\n\n" +

                "Welcome to the help section! Here's how you can use the bot:\n\n" +

                "✨*Commands:*\n\n"+

                "➖*/start* - Get an introduction and instructions on how to use the bot.\n"+
                "➖*/from <currency code or country name>* – Set your input currency.\n"+
                "   Example: `/from MYR` or `/from Malaysia`\n"+
                "➖*/to <currency code or country name>* – Set your output currency.\n"+
                "   Example: `/to SGD` or `/to Singapore`\n"+
                "➖*/getcurrencies* – View your selected input and output currency.\n"+
                "➖*/deletecurrency* – Delete your output currency.\n\n"+

                "⚡️ *Additional Features:*\n\n"+

                "➖*Location-based currency setting:* Send your location 🌍 to automatically detect your input currency based on where you are.\n" +
                "➖*Currency codes & country names:* You can use either currency codes (e.g., SGD, MYR) or country names (e.g., Singapore, Malaysia) to set the currencies.\n\n" +

                "Once you've set the input and output currencies, simply type the amount you'd like to convert, and I'll handle the rest 💡.\n\n" +

                "If you need more help, just ask! 💬";
    }
}
