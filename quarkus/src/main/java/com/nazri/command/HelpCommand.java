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
        return "Welcome to *Currency Coinverter Bot* 🌍💱\n" +
                "I can help you with quick and easy currency conversions! Here's how you can get started:\n\n" +
                "1. *Set your input and output currencies:* \n" +
                "Use `/from <currency>` and `/to <currency>` to set your preferred currencies.\n" +
                "_Examples:_\n" +
                "`/from MYR` 🇲🇾 or `/from Malaysia`\n" +
                "`/to SGD` 🇸🇬 or `/to Singapore`\n\n" +
                "2. *Send your amount:*\n" +
                "Once your currencies are set, just send the amount, and I’ll do the rest! 💡\n\n" +
                "3. *Store up to 3 currencies:* \n" +
                "You can save up to 3 output currencies for quick conversions.\n" +
                "Use `/getcurrencies` to see them, and `/deletecurrency` to remove any.\n\n" +
                "4. *Set input currency via location:* \n" +
                "Send your location 🌍 to automatically set your input currency based on where you are.\n\n" +
                "Let me know if you need more help! 😊";







    }
}
