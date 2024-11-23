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
public class FromCurrencyCommand implements Command {

    private static final Logger log = Logger.getLogger(ToCurrencyCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    CurrencyService currencyService;

    @Override
    public String getName() {
        return "from";
    }

    /**
     * Users select input currency
     *
     * @param message
     * @param args
     */
    @Override
    public void execute(Message message, String args) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setParseMode(Constant.MARKDOWN);

        String currencyCode = currencyService.getCurrencyCode(args);
        try {

            if (currencyCode == null) {
                response.setText("Please try again, you've entered invalid currency code / country");
                telegramBot.execute(response);
            } else {
                User user = userService.findOne(message.getChatId());
                user.setInputCurrency(currencyCode);
                userService.update(user);
                response.setText("New Input Currency Code Saved: " + currencyCode);
                telegramBot.execute(response);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}