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

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FromCurrencyCommand implements Command {

    private static final Logger log = Logger.getLogger(ToCurrencyCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    CurrencyService currencyService;

    @Inject
    MessageService messageService;

    @ConfigProperty(name = "top.input.currencies")
    List<String> inputCurrencies;

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
        String currencyCode = currencyService.getCurrencyCode(args);
        
        try {
            TelegramResponse response;
            
            if (currencyCode == null) {
                response = messageService.createResponse("from.currency.invalid")
                        .keyboard(KeyboardUtil.createCurrencyKeyboard(inputCurrencies, getName()));
            } else {
                User user = userService.findOne(message.getChatId());
                user.setInputCurrency(currencyCode);
                userService.update(user);
                
                response = messageService.createResponse("from.currency.set", 
                        Util.getEmojiFlag(currencyCode), currencyCode);
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

}
