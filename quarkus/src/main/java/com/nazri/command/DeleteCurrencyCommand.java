package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.TelegramResponse;
import com.nazri.service.UserService;
import com.nazri.util.Constant;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DeleteCurrencyCommand implements Command {

    private static final Logger log = Logger.getLogger(DeleteCurrencyCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    MessageService messageService;

    @Override
    public String getName() {
        return "deletecurrency";
    }

    /**
     * Users to delete Output currencies
     *
     * @param message
     * @param args
     */
    @Override
    public void execute(Message message, String args) {
        User user = userService.findOne(message.getChatId());

        try {
            TelegramResponse response;

            if (user.getOutputCurrency().isEmpty()) {
                response = messageService.createResponse("delete.currency.none");
            } else {
                response = messageService.createResponse("delete.currency.select")
                        .keyboard(createCurrencyKeyboard(user.getOutputCurrency()));
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

            TelegramResponse response = messageService.createResponse("delete.currency.success", 
                    Util.getEmojiFlag(data), data);

            User user = userService.findOne(callbackQuery.getMessage().getChatId());
            if(user.getOutputCurrency().remove(data)) {
                userService.update(user);
            }

            telegramBot.execute(response.toMessage(callbackQuery.getMessage().getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup createCurrencyKeyboard(List<String> currencies) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String currencyCode : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(Util.getEmojiFlag(currencyCode) + " " + currencyCode);
            button.setCallbackData(getName() + ":" + currencyCode);
            rowInline.add(button);
        }

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
