package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import com.nazri.util.Constant;
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
        response.setParseMode(Constant.MARKDOWN);

        User user = userService.findOne(message.getChatId());

        response.setText("Input Currency: " + user.getInputCurrency() + "\n" +
                "Output Currency: " + String.valueOf(user.getOutputCurrency()) + "\n" +
                "You can use /to and /from commands to add your currencies.");
        try {
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
