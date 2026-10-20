package com.nazri.service;

import com.nazri.command.Command;
import com.nazri.command.CommandRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotTest {

    @Mock
    private CommandRegistry commandRegistry;

    @Mock
    private Command command;

    @Mock
    private Update update;

    @Mock
    private CallbackQuery callbackQuery;

    private TestableTelegramBot telegramBot;

    static class TestableTelegramBot extends TelegramBot {
        BotApiMethod<?> executedMethod;
        boolean throwOnExecute = false;

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
            if (throwOnExecute) {
                throw new TelegramApiException("Simulated TelegramApiException");
            }
            this.executedMethod = method;
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        telegramBot = new TestableTelegramBot();
        telegramBot.botUsername = "test_bot";
        telegramBot.botToken = "test_token";
        telegramBot.webhookUrl = "https://example.com/webhook";
        telegramBot.commandRegistry = commandRegistry;
    }

    @Test
    void getBotProperties_ShouldReturnConfiguredValues() {
        assertEquals("test_bot", telegramBot.getBotUsername());
        assertEquals("test_token", telegramBot.getBotToken());
        assertEquals("https://example.com/webhook", telegramBot.getBotPath());
    }

    @Test
    void onWebhookUpdateReceived_ShouldExecuteKnownCommand_WhenCommandMessageReceived() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("/from USD");

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("from")).thenReturn(command);

        telegramBot.onWebhookUpdateReceived(update);

        verify(commandRegistry).getCommand("from");
        verify(command).execute(message, "USD");
    }

    @Test
    void onWebhookUpdateReceived_ShouldExecuteKnownCommandWithoutArgs_WhenSimpleCommandReceived() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("/start");

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("start")).thenReturn(command);

        telegramBot.onWebhookUpdateReceived(update);

        verify(commandRegistry).getCommand("start");
        verify(command).execute(message, "");
    }

    @Test
    void onWebhookUpdateReceived_ShouldSendUnknownMessage_WhenCommandNotFound() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("/unknown");

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("unknown")).thenReturn(null);

        telegramBot.onWebhookUpdateReceived(update);

        assertNotNull(telegramBot.executedMethod);
        assertInstanceOf(SendMessage.class, telegramBot.executedMethod);
        SendMessage sent = (SendMessage) telegramBot.executedMethod;
        assertEquals("12345", sent.getChatId());
        assertTrue(sent.getText().contains("didn't quite catch that"));
    }

    @Test
    void onWebhookUpdateReceived_ShouldSendErrorMessage_WhenCommandThrowsException() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("/help");

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("help")).thenReturn(command);
        doThrow(new RuntimeException("Execution failure")).when(command).execute(any(), any());

        telegramBot.onWebhookUpdateReceived(update);

        assertNotNull(telegramBot.executedMethod);
        assertInstanceOf(SendMessage.class, telegramBot.executedMethod);
        SendMessage sent = (SendMessage) telegramBot.executedMethod;
        assertEquals("12345", sent.getChatId());
        assertTrue(sent.getText().contains("Execution failure"));
    }

    @Test
    void onWebhookUpdateReceived_ShouldPrependConvertCommand_WhenRegularNumericMessageReceived() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("100");

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("convert")).thenReturn(command);

        telegramBot.onWebhookUpdateReceived(update);

        assertEquals("/convert 100", message.getText());
        verify(commandRegistry).getCommand("convert");
        verify(command).execute(message, "100");
    }

    @Test
    void onWebhookUpdateReceived_ShouldProcessLocation_WhenCoordinatesResolveCountry() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        Location location = new Location();
        // Kuala Lumpur coordinates (Malaysia)
        location.setLatitude(3.1390);
        location.setLongitude(101.6869);
        message.setLocation(location);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("from")).thenReturn(command);

        telegramBot.onWebhookUpdateReceived(update);

        assertNotNull(message.getText());
        assertTrue(message.getText().startsWith("/from"));
        verify(commandRegistry).getCommand("from");
        verify(command).execute(eq(message), anyString());
    }

    @Test
    void onWebhookUpdateReceived_ShouldSendErrorMessage_WhenLocationProcessingFails() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        Location location = new Location();
        // Out of bounds coordinates that cause Coordinates2Country or processing to fail
        location.setLatitude(999.0);
        location.setLongitude(999.0);
        message.setLocation(location);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);

        telegramBot.onWebhookUpdateReceived(update);

        assertNotNull(telegramBot.executedMethod);
        assertInstanceOf(SendMessage.class, telegramBot.executedMethod);
        SendMessage sent = (SendMessage) telegramBot.executedMethod;
        assertTrue(sent.getText().contains("Failed to process location data") || sent.getText().contains("Error determining country"));
    }

    @Test
    void onWebhookUpdateReceived_ShouldDelegateToCallbackHandler_WhenCallbackReceived() {
        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("convert:50");
        when(commandRegistry.getCommand("convert")).thenReturn(command);

        telegramBot.onWebhookUpdateReceived(update);

        verify(commandRegistry).getCommand("convert");
        verify(command).handleCallback(callbackQuery, "50");
    }

    @Test
    void onWebhookUpdateReceived_ShouldSendErrorMessage_WhenCallbackHandlerThrows() {
        Message cbMessage = new Message();
        cbMessage.setChat(new Chat(12345L, "private"));

        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("convert:error");
        when(callbackQuery.getMessage()).thenReturn(cbMessage);
        when(commandRegistry.getCommand("convert")).thenReturn(command);
        doThrow(new RuntimeException("Callback failure")).when(command).handleCallback(any(), any());

        telegramBot.onWebhookUpdateReceived(update);

        assertNotNull(telegramBot.executedMethod);
        assertInstanceOf(SendMessage.class, telegramBot.executedMethod);
        SendMessage sent = (SendMessage) telegramBot.executedMethod;
        assertTrue(sent.getText().contains("Callback failure"));
    }

    @Test
    void onWebhookUpdateReceived_ShouldProcessEditedMessage_WithoutError() {
        Message editedMsg = new Message();
        editedMsg.setChat(new Chat(12345L, "private"));
        editedMsg.setText("Edited text");

        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasEditedMessage()).thenReturn(true);
        when(update.getEditedMessage()).thenReturn(editedMsg);

        assertDoesNotThrow(() -> telegramBot.onWebhookUpdateReceived(update));
    }

    @Test
    void onWebhookUpdateReceived_ShouldProcessChannelPost_WithoutError() {
        Message channelMsg = new Message();
        channelMsg.setChat(new Chat(99999L, "channel"));

        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasEditedMessage()).thenReturn(false);
        when(update.hasChannelPost()).thenReturn(true);
        when(update.getChannelPost()).thenReturn(channelMsg);

        assertDoesNotThrow(() -> telegramBot.onWebhookUpdateReceived(update));
    }

    @Test
    void onWebhookUpdateReceived_ShouldProcessInlineQuery_WithoutError() {
        InlineQuery inlineQuery = new InlineQuery();
        inlineQuery.setQuery("100 USD to MYR");

        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasEditedMessage()).thenReturn(false);
        when(update.hasChannelPost()).thenReturn(false);
        when(update.hasInlineQuery()).thenReturn(true);
        when(update.getInlineQuery()).thenReturn(inlineQuery);

        assertDoesNotThrow(() -> telegramBot.onWebhookUpdateReceived(update));
    }

    @Test
    void onWebhookUpdateReceived_ShouldWrapTelegramApiException_InRuntimeException() {
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        message.setText("/unknown");

        telegramBot.throwOnExecute = true;

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(commandRegistry.getCommand("unknown")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> telegramBot.onWebhookUpdateReceived(update));
    }
}
