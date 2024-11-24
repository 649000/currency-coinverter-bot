package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.TelegramBot;
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
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setParseMode(Constant.MARKDOWN);
        User user = userService.findOne(message.getChatId());
        try {

            if (user.getOutputCurrency().isEmpty()) {
                response.setText("It looks like you don’t have any currencies to delete yet. 🤔\n" +
                        "To get started, add your desired currency using the `/to` command. \n" +
                        "_Example:_ \n" +
                        "`/to SG`, `/to SGD` or `/to Singapore`.");

                telegramBot.execute(response);
            } else {
                response.setText("Which currency would you like to delete? 💭");

                // Create inline keyboard
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                // First row of buttons
                List<InlineKeyboardButton> rowInline = new ArrayList<>();


                for (String currencyCode : user.getOutputCurrency()) {
                    InlineKeyboardButton button1 = new InlineKeyboardButton();
                    button1.setText(Util.getFlagFromCurrencyCode(currencyCode) +" "+currencyCode);
                    button1.setCallbackData(getName() + ":" + currencyCode);
                    rowInline.add(button1);
                }

                // Add the row to rows list
                rowsInline.add(rowInline);

                // Set the keyboard to the message
                markupInline.setKeyboard(rowsInline);

                response.setReplyMarkup(markupInline);
                telegramBot.execute(response);
            }
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

            SendMessage response = new SendMessage();
            response.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));
            response.setParseMode(Constant.MARKDOWN);
            response.setText("The currency "+ Util.getFlagFromCurrencyCode(data) +"*" + data + "* has been deleted ✅.");

            User user = userService.findOne(callbackQuery.getMessage().getChatId());
            if(user.getOutputCurrency().remove(data)) {
                userService.update(user);
            }

            telegramBot.execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
