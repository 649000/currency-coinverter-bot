package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.model.User;
import com.nazri.service.CurrencyService;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@ApplicationScoped
public class ToCurrencyCommand implements Command {

    private static final Logger log = Logger.getLogger(ToCurrencyCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    CurrencyService currencyService;

    @Inject
    MessageService messageService;

    @ConfigProperty(name = "top.output.currencies")
    List<String> outputCurrencies;

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

        try {
            TelegramResponse response;
            String currencyCode = currencyService.getCurrencyCode(args);
            if (args == null || args.isEmpty()) {
                response = messageService.createResponse("to.currency.empty")
                        .keyboard(KeyboardUtil.createCurrencyKeyboard(outputCurrencies, getName()));
            }else if (currencyCode == null) {
                response = messageService.createResponse("to.currency.invalid")
                        .keyboard(KeyboardUtil.createCurrencyKeyboard(outputCurrencies, getName()));
            } else {
                User user = userService.findOne(message.getChatId());

                if (user.getOutputCurrency().size() >= 3) {
                    response = messageService.createResponse("to.currency.limit")
                            .keyboard(KeyboardUtil.createCurrencyKeyboard(user.getOutputCurrency(), "deletecurrency"));
                } else {
                    user.getOutputCurrency().add(currencyCode);
                    userService.update(user);

                    String outputCurrenciesList = buildOutputCurrenciesList(user.getOutputCurrency());
                    response = messageService.createResponse("to.currency.set",
                            Util.getEmojiFlag(currencyCode), currencyCode, outputCurrenciesList);
                }
            }

            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildOutputCurrenciesList(List<String> outputCurrencies) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < outputCurrencies.size(); i++) {
            builder.append(i + 1).append(". ")
                    .append(Util.getEmojiFlag(outputCurrencies.get(i)))
                    .append(" ").append(outputCurrencies.get(i)).append(" \n");
        }
        return builder.toString();
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
