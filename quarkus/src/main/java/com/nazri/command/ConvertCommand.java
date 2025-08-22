package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.CurrencyService;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.TelegramResponse;
import com.nazri.service.UserService;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
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

    @Inject
    MessageService messageService;

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public void execute(Message message, String args) {
        try {
            if (!Util.isNumeric(args)) {
                TelegramResponse response = messageService.createResponse("convert.invalid.numeric");
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            User user = userService.findOne(message.getChatId());
            if (user.getInputCurrency() == null) {
                TelegramResponse response = messageService.createResponse("convert.missing.input.currency");
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            if (user.getOutputCurrency().isEmpty()) {
                TelegramResponse response = messageService.createResponse("convert.missing.output.currency");
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            BigDecimal inputAmount = BigDecimal.valueOf(Double.parseDouble(args));
            Map<String, BigDecimal> result = currencyService.convertCurrency(inputAmount, user.getInputCurrency(), user.getOutputCurrency());

            StringBuilder sb = new StringBuilder();
            sb.append("💵*Coinverted Currencies* 💵\n\n");

            sb.append("*From*\n");
            sb.append(Util.getEmojiFlag(user.getInputCurrency())).append(" ").append(Util.formatMoney(inputAmount, user.getInputCurrency())).append("\n\n");

            sb.append("*To*\n");
            for (String currencyCode : user.getOutputCurrency()) {
                if (result.containsKey(currencyCode)) {
                    sb.append(Util.getEmojiFlag(currencyCode))
                            .append(Util.formatMoney(
                                    result.get(currencyCode),
                                    currencyCode
                            ))
                            .append("\n");
                }
            }
            
            TelegramResponse response = TelegramResponse.builder().text(sb.toString());
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
