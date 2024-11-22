package com.nazri.service;

import com.nazri.command.Command;
import com.nazri.command.CommandRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TelegramBot extends TelegramWebhookBot {

    private static final Logger log = Logger.getLogger(TelegramBot.class);

    @ConfigProperty(name = "telegram.bot.username")
    String botUsername;

    @ConfigProperty(name = "telegram.bot.token")
    String botToken;

    @ConfigProperty(name = "telegram.webhook.url")
    String webhookUrl;

    @Inject
    CommandRegistry commandRegistry;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            processMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
        } else if (update.hasEditedMessage()) {
            processEditedMessage(update.getEditedMessage());
        } else if (update.hasChannelPost()) {
            handleChannelPost(update);
        } else if (update.hasInlineQuery()) {
            handleInlineQuery(update);
        }

        return null;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return webhookUrl;
    }


    public void processMessage(Message message) {
        try {
            if (message.getText() != null) {
                if (message.getText().startsWith("/")) {
                    processCommand(message);
                } else {
                    processRegularMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Error processing message: ", e);
            sendErrorMessage(message.getChatId(), e.getMessage());
        }
    }

    private void processCommand(Message message) {
        log.infof("Processing Command: %s", message.getText());
        String[] parts = message.getText().split("\\s+", 2);
        String commandName = parts[0].substring(1).toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        Command command = commandRegistry.getCommand(commandName);
        if (command != null) {
            try {
                command.execute(message, args.trim());
            } catch (Exception e) {
                log.error("Error executing command: " + commandName, e);
                sendErrorMessage(message.getChatId(), e.getMessage());
            }
        } else {
//            sendUnknownCommandMessage(message.chat().id());
        }
    }

    private void processRegularMessage(Message message) {
        log.infof("Processing Regular Message: %s", message.getText());
        String body = String.format("Got it! You said: \n\n *%s* \n\n" +
                "If you'd like to share more, I'm here to listen!", message.getText());


        SendMessage outputMessage = new SendMessage(message.getChatId().toString(), body);
        try {
            execute(outputMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void processCallbackQuery(CallbackQuery callbackQuery) {
        log.infof("Processing Callback: %s", callbackQuery.getData());
        try {
            // Process callback data
            String[] parts = callbackQuery.getData().split(":");
            String commandName = parts[0];
            String data = parts.length > 1 ? parts[1] : "";

            Command command = commandRegistry.getCommand(commandName);
            if (command != null) {
                command.handleCallback(callbackQuery, data);
            }
        } catch (Exception e) {
            log.error("Error processing callback query: ", e);
            sendErrorMessage(callbackQuery.getMessage().getChatId(), e.getMessage());
        }
    }

    public void processEditedMessage(Message message) {
        log.infof("Processing Edited Message: %s", message.getText());
        SendMessage outputMessage = new SendMessage(message.getChatId().toString(), "Received your edited message");
//        bot.execute(outputMessage);
    }

    private void sendErrorMessage(Long chatId, String text) {
        String body = "Oops, something went wrong! 😕\n\n" +
                "Don't worry, please try again later and we'll get things back on track. 😊\n\n" +
                "If the issue persists, here's the info for debugging:\n" +
                text;

        SendMessage message = new SendMessage(chatId.toString(), body);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendUnknownCommandMessage(Long chatId) {
        String body = "Hmm, I didn't quite catch that. 😅\n\n" +
                "No worries! Type /help to see the list of available commands. I'm here to assist! 😊";

        SendMessage message = new SendMessage(String.valueOf(chatId), body);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


//    private SendMessage handleMessage(Update update) {
//        long chatId = update.getMessage().getChatId();
//        String text = update.getMessage().getText();
//
//        log.infof("Received message from %d: %s", chatId, text);
//
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText("You said: " + text);
//
//        // Create inline keyboard
//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//
//        // First row of buttons
//        List<InlineKeyboardButton> rowInline = new ArrayList<>();
//
//        // Create buttons
//        InlineKeyboardButton button1 = new InlineKeyboardButton();
//        button1.setText("Option 1");
//        button1.setCallbackData("OPTION1_CALLBACK");
//
//        InlineKeyboardButton button2 = new InlineKeyboardButton();
//        button2.setText("Option 2");
//        button2.setCallbackData("OPTION2_CALLBACK");
//
//        // Add buttons to the row
//        rowInline.add(button1);
//        rowInline.add(button2);
//
//        // Add the row to rows list
//        rowsInline.add(rowInline);
//
//        // Set the keyboard to the message
//        markupInline.setKeyboard(rowsInline);
//        message.setReplyMarkup(markupInline);
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//        return message;
//    }


    /**
     * TODO:
     *
     * @param update
     */
    private void handleChannelPost(Update update) {
        long chatId = update.getChannelPost().getChatId();
        log.infof("Received channel post from %d", chatId);
    }

    /**
     * TODO:
     *
     * @param update
     */
    private void handleInlineQuery(Update update) {
        String query = update.getInlineQuery().getQuery();
        log.infof("Received inline query: %s", query);
    }
}
