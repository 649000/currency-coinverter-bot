package com.nazri.service;

import com.nazri.command.Command;
import com.nazri.command.CommandRegistry;
import com.nazri.util.Constant;
import com.nazri.util.Util;
import io.github.coordinates2country.Coordinates2Country;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class TelegramBot extends TelegramWebhookBot {

    private static final Logger log = Logger.getLogger(TelegramBot.class);

    @ConfigProperty(name = "telegram.bot.username" )
    String botUsername;

    @ConfigProperty(name = "telegram.bot.token" )
    String botToken;

    @ConfigProperty(name = "telegram.webhook.url" )
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

    /**
     * Process messages sent by user
     * @param message
     */
    private void processMessage(Message message) {
        try {
            if (message.getText() != null) {
                if (message.getText().startsWith("/" )) {
                    processCommand(message);
                } else {
                    processRegularMessage(message);
                }
            } else if (message.getLocation() != null) {
                processLocation(message);
            } else {
                log.warn("Unknown Message Type: " + message);
            }
        } catch (Exception e) {
            log.error("Error processing message: ", e);
            sendErrorMessage(message.getChatId(), e.getMessage());
        }
    }

    /**
     * Process Command related messages sent by user. Eg. /start or /help
     * @param message
     */
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
            sendUnknownCommandMessage(message.getChatId());
        }
    }

    /**
     * Process plain text messages sent by user
     * @param message
     */
    private void processRegularMessage(Message message) {
        log.infof("Processing Regular Message: %s", message.getText());

        if (Util.isNumeric(message.getText())) {
            message.setText("/convert " + message.getText());
            processCommand(message);
            return;
        } else {
            log.info("Regular Message Received: " + message.getText());
            message.setText("/help" );
            processCommand(message);
        }
    }

    /**
     * Process Location sent by user
     * @param message
     */
    private void processLocation(Message message) {
        String country = Coordinates2Country.country(message.getLocation().getLatitude(), message.getLocation().getLongitude());
        message.setText("/from " + country);
        processCommand(message);
    }

    /**
     * Process Callbacks by user in Inline Keyboard
     * @param callbackQuery
     */
    private void processCallbackQuery(CallbackQuery callbackQuery) {
        log.infof("Processing Callback: %s", callbackQuery.getData());
        try {
            // Process callback data
            String[] parts = callbackQuery.getData().split(":" );
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

    /**
     * Process Edited messages by user
     * @param message
     */
    private void processEditedMessage(Message message) {
        log.infof("Processing Edited Message: %s", message.getText());
        SendMessage response = new SendMessage(String.valueOf(message.getChatId()), "Received your edited message" );
    }

    /**
     * Generic error messages for debugging
     * @param chatId
     * @param text
     */
    private void sendErrorMessage(Long chatId, String text) {
        String body = "Oops, something went wrong! 😕\n\n" +
                "Don't worry, please try again later and we'll get things back on track. 😊\n\n" +
                "If the issue persists, here's the info for debugging:\n" +
                text;

        SendMessage response = new SendMessage(String.valueOf(chatId), body);
        response.setParseMode(Constant.MARKDOWN);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendUnknownCommandMessage(Long chatId) {
        String body = "Hmm, I didn't quite catch that. 😅\n\n" +
                "No worries! Type /help to see the list of available commands. I'm here to assist! 😊";

        SendMessage response = new SendMessage(String.valueOf(chatId), body);
        response.setParseMode(Constant.MARKDOWN);

        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
