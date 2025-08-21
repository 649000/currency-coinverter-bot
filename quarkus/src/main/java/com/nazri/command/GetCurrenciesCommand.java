package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import com.nazri.util.Constant;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class GetCurrenciesCommand implements Command {

    private static final Logger log = Logger.getLogger(GetCurrenciesCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Override
    public String getName() {
        return "getcurrencies";
    }

    @Override
    public void execute(Message message, String args) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setParseMode(Constant.HTML);

        User user = userService.findOne(message.getChatId());

        StringBuilder body = new StringBuilder();
        // Add input currency
        body.append("Your Input Currency:\n");
        if (user.getInputCurrency() == null) {
            body.append("\n\n");
        } else {
            body.append("1. ").append(Util.getEmojiFlag(user.getInputCurrency())).append(" <b>").append(user.getInputCurrency()).append("</b>\n\n");
        }


        // Add output currencies
        body.append("Your Output Currencies:\n");
        for (int i = 0; i < user.getOutputCurrency().size(); i++) {
            body.append(i + 1).append(". ").append(Util.getEmojiFlag(user.getOutputCurrency().get(i))).append(" <b>").append(user.getOutputCurrency().get(i)).append("</b> \n");
        }


        if (user.getInputCurrency() == null || user.getOutputCurrency().isEmpty()) {
            body.append("\n\n Use /help to know more on setting your input/output currencies");
        }
        response.setText(body.toString());

        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
