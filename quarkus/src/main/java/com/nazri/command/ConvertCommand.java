package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.CurrencyService;
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

import java.math.BigDecimal;
import java.util.Map;

@ApplicationScoped
public class ConvertCommand implements Command {

    private static final Logger log = Logger.getLogger(ConvertCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    CurrencyService currencyService;

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public void execute(Message message, String args) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setParseMode(Constant.MARKDOWN);
        try {
            if (!Util.isNumeric(args)) {
                //Input is not numeric
                response.setText("Please enter a numeric value to convert .");
                telegramBot.execute(response);
                return;
            }

            User user = userService.findOne(message.getChatId());
            if (user.getInputCurrency() == null) {

                response.setText("Please set an input currency to convert from. 💡\n" +
                        "You can use the `/from` command. \n" +
                        "Examples: `/from MYR`, `/from MY`, or `/from Malaysia` 🇲🇾.\n" +
                        "Alternatively, you can simply send us your location, and we'll determine your input currency based on that. 🌍");

                telegramBot.execute(response);
                return;
            }

            if (user.getOutputCurrency().isEmpty()) {
                response.setText("Please set an output currency to convert to. 💡\n" +
                        "You can use the `/to` command. \n" +
                        "Examples: `/to SGD`, `/to SG`, or `/to Singapore` 🇸🇬.");

                telegramBot.execute(response);
                return;
            }

            Map<String, BigDecimal> result = currencyService.convertCurrency(BigDecimal.valueOf(Long.parseLong(args)), user.getInputCurrency(), user.getOutputCurrency());

            StringBuilder sb = new StringBuilder();
            sb.append("💵*Coinverted Currencies* 💵\n\n");
            sb.append("*From*\n");
            sb.append(user.getInputCurrency()).append(" ").append(args).append("\n\n");
            sb.append("*To*\n");
            for (String currencyCode : user.getOutputCurrency()) {
                if (result.containsKey(currencyCode)) {
                    sb.append(currencyCode).append(" ").append(result.get(currencyCode)).append("\n");
                }
            }
            response.setText(sb.toString());
            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
