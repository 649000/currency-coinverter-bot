package com.nazri.command;

import com.nazri.model.User;
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

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GetCurrenciesCommand implements Command {

    private static final Logger log = Logger.getLogger(GetCurrenciesCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    MessageService messageService;

    @ConfigProperty(name = "top.output.currencies")
    List<String> outputCurrencies;

    @ConfigProperty(name = "top.input.currencies")
    List<String> inputCurrencies;

    @Override
    public String getName() {
        return "getcurrencies";
    }

    @Override
    public void execute(Message message, String args) {
        User user = userService.findOne(message.getChatId());
        
        try {
            TelegramResponse response;
            
            if (user.getInputCurrency() == null && user.getOutputCurrency().isEmpty()) {
                response = messageService.createResponse("getcurrencies.empty");
            } else if (user.getInputCurrency() == null) {
                String outputList = formatOutputCurrencies(user.getOutputCurrency());
                response = messageService.createResponse("getcurrencies.no.input", outputList)
                        .keyboard(createCurrencyKeyboard(inputCurrencies, true));
            } else if (user.getOutputCurrency().isEmpty()) {
                response = messageService.createResponse("getcurrencies.no.output", 
                        Util.getEmojiFlag(user.getInputCurrency()), user.getInputCurrency())
                        .keyboard(createCurrencyKeyboard(outputCurrencies, false));
            } else {
                String outputList = formatOutputCurrencies(user.getOutputCurrency());
                response = messageService.createResponse("getcurrencies.complete", 
                        Util.getEmojiFlag(user.getInputCurrency()), user.getInputCurrency(), outputList);
            }
            
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

    private String formatOutputCurrencies(List<String> currencies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currencies.size(); i++) {
            sb.append(i + 1)
              .append(". ")
              .append(Util.getEmojiFlag(currencies.get(i)))
              .append(" *")
              .append(currencies.get(i))
              .append("*\n");
        }
        return sb.toString();
    }

    private InlineKeyboardMarkup createCurrencyKeyboard(List<String> currencies, boolean inputCurrency) {
        String commandPrefix = inputCurrency ? "from" : "to";
        return KeyboardUtil.createCurrencyKeyboard(currencies, commandPrefix);
    }

}
