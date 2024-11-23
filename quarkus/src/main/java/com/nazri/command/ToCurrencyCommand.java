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
public class ToCurrencyCommand implements Command {

    private static final Logger log = Logger.getLogger(ToCurrencyCommand.class);

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

        String currencyCode = currencyService.getCurrencyCode(args);
        try {

            if (currencyCode == null) {
                String body = "Oops, it looks like you've entered an invalid currency code or country! 😕\n\n" +
                        "Please use a valid currency code (e.g., `SGD`, `MYR`, `JPY`) or a full country name (e.g., `Singapore`, `Malaysia`, `Japan`). \n\n" +
                        "_Example:_ \n" +
                        "`/to SGD` 🇸🇬 or `/to Singapore`.";

                response.setText(body);
                telegramBot.execute(response);
            } else {
                User user = userService.findOne(message.getChatId());

                if(user.getOutputCurrency().size() >= 3 ){
                    response.setText(
                      "Please delete your stored output currency before adding more.\n\n" +
                      "Use /deletecurrency to delete currencies"
                    );
                } else {
                    user.getOutputCurrency().add(currencyCode);
                    userService.update(user);
                    response.setText(
                            "Your new output currency has been saved as *"+ currencyCode +"*. \n" +
                                    "You can now use this currency for conversions! \n\n" +
                                    "Output Currencies: *" + String.join(", ", user.getOutputCurrency()) + "*"
                    );
                }

                telegramBot.execute(response);
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
