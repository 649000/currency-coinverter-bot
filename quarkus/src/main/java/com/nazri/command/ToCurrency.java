package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.CurrencyService;
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
public class ToCurrency implements Command {

    private static final Logger log = Logger.getLogger(ToCurrency.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    CurrencyService currencyService;

    @Override
    public String getName() {
        return "to";
    }

    /**
     * Users select currency output
     *
     * @param message
     * @param args
     */
    @Override
    public void execute(Message message, String args) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setParseMode(Constant.MARKDOWN);

        String currencyCode = currencyService.getCurrencyCode(message.getText());
        try {

            if (currencyCode == null) {
                response.setText("Please try again, you've entered invalid currency code / country");
                telegramBot.execute(response);
            } else {
                User user = userService.findOne(message.getChatId());
                user.getOutputCurrency().add(currencyCode);
                userService.update(user);
                response.setText("New Output Currency Code Saved: " + currencyCode);
                telegramBot.execute(response);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
