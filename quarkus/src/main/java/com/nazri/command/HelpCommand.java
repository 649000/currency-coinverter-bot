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
        response.setParseMode(Constant.MARKDOWNV2);

        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBody() {
        return "• Use `/to` and `/from` with currency codes or country names\\. \n" +
                "  _Example:_ \n" +
                "`/from MYR` 🇲🇾 or `/from Malaysia`\\. \n" +
                "`/to SGD` 🇸🇬 or `/to Singapore` \n" +
                "• Send me the amount, and I’ll handle the rest 💡\\. \n\n" +
                "Let’s get started 🚀\\!";
    }
}
