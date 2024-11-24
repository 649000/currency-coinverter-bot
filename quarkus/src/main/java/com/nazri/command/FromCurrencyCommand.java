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
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
                String body = "Oops, it looks like you've entered an invalid currency code or country! 😕\n\n" +
                        "Please use a valid currency code (e.g., `SGD`, `MYR`, `JPY`) or a full country name (e.g., `Singapore`, `Malaysia`, `Japan`). \n\n" +
                        "*Example:*\n" +
                        "`/from MY`, `/from MYR` or `/from Malaysia`.\n\n" +
                        "Alternatively, send your location 🌍 to automatically set your input currency based on where you are.\n\n" +
                        "Or choose one of the common currencies below:";

                response.setText(body);
                response.setReplyMarkup(setInlineKeyboard());
            } else {
                User user = userService.findOne(message.getChatId());
                user.setInputCurrency(currencyCode);
                userService.update(user);
                response.setText(
                        "Your input currency has been saved:\n"+ Util.getFlagFromCurrencyCode(currencyCode) +" *" + currencyCode + "*. \n" +
                                "You can now use this currency for conversions!");
            }

            telegramBot.execute(response);
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

    private InlineKeyboardMarkup setInlineKeyboard() {
        // Create inline keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // First row of buttons
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String currencyCode : Util.topInputCurrencies().keySet()) {
            String flag = Util.topInputCurrencies().get(currencyCode);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(flag + " " + currencyCode.toUpperCase());
            button.setCallbackData(getName() + ":" + currencyCode);
            rowInline.add(button);
        }

        // Add the row to rows list
        rowsInline.add(rowInline);

        // Set the keyboard to the message
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
}
