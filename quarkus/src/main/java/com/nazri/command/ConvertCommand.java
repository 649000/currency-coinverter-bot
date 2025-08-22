package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.CurrencyService;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.model.TelegramResponse;
import com.nazri.service.UserService;
import com.nazri.util.KeyboardUtil;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @ConfigProperty(name = "common.amount")
    List<String> commonAmount;

    @ConfigProperty(name = "top.output.currencies")
    List<String> outputCurrencies;

    @ConfigProperty(name = "top.input.currencies")
    List<String> inputCurrencies;

    @ConfigProperty(name = "multiplier.input")
    List<String> multiplierList;

    @ConfigProperty(name = "multiplier.symbol")
    List<String> multiplierSymbols;

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public void execute(Message message, String args) {
        try {
            if (!Util.isNumeric(args)) {
                TelegramResponse response = messageService.createResponse("convert.invalid.numeric")
                        .keyboard(KeyboardUtil.createAmountKeyboard(commonAmount, getName()));
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            User user = userService.findOne(message.getChatId());
            if (user.getInputCurrency() == null) {
                TelegramResponse response = messageService.createResponse("convert.missing.input.currency")
                        .keyboard(KeyboardUtil.createCurrencyKeyboard(inputCurrencies, "from"));
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            if (user.getOutputCurrency().isEmpty()) {
                TelegramResponse response = messageService.createResponse("convert.missing.output.currency")
                        .keyboard(KeyboardUtil.createCurrencyKeyboard(outputCurrencies, "to"));
                telegramBot.execute(response.toMessage(message.getChatId()));
                return;
            }

            BigDecimal inputAmount = BigDecimal.valueOf(Double.parseDouble(args));
            Map<String, BigDecimal> result = currencyService.convertCurrency(inputAmount, user.getInputCurrency(), user.getOutputCurrency());

            String fromCurrency = Util.getEmojiFlag(user.getInputCurrency()) + " " + 
                                 Util.formatMoney(inputAmount, user.getInputCurrency());

            StringBuilder toCurrencies = new StringBuilder();
            for (String currencyCode : user.getOutputCurrency()) {
                if (result.containsKey(currencyCode)) {
                    toCurrencies.append(Util.getEmojiFlag(currencyCode))
                               .append(Util.formatMoney(result.get(currencyCode), currencyCode))
                               .append("\n");
                }
            }

            TelegramResponse response = messageService.createResponse("convert.result", 
                fromCurrency, toCurrencies.toString())
                    .keyboard(KeyboardUtil.createMultiplierKeyboard(inputAmount, multiplierList, multiplierSymbols, getName()));
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleCallback(CallbackQuery callbackQuery, String data) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
        try {
            telegramBot.execute(answerCallbackQuery);

            Message message = new Message();
            Chat chat = new Chat(callbackQuery.getMessage().getChatId(), "private");
            message.setChat(chat);
            execute(message, data);

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
