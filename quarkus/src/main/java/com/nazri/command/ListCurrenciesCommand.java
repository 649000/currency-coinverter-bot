package com.nazri.command;

import com.nazri.service.TelegramBot;
import com.nazri.util.Constant;
import jakarta.inject.Inject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ListCurrenciesCommand implements Command{
    @Inject
    TelegramBot telegramBot;

    @Override
    public String getName() {
        return "listcurrencies";
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


    // TODO:
    private String getBody() {
        return "Below are the list of supported currencies: \n" +
                "";


    }

}
