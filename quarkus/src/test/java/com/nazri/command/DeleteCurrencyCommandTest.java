package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.model.User;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import com.nazri.util.KeyboardUtil;
import com.nazri.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCurrencyCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private MessageService messageService;

    @Mock
    private Message message;

    @Mock
    private User user;

    @Mock
    private TelegramResponse telegramResponse;

    @Mock
    private SendMessage sendMessage;

    @Mock
    private Message executedMessage;

    @Mock
    private InlineKeyboardMarkup keyboard;

    @Mock
    private CallbackQuery callbackQuery;

    @Mock
    private Message callbackMessage;

    @Mock
    private AnswerCallbackQuery answerCallbackQuery;

    private DeleteCurrencyCommand deleteCurrencyCommand;

    @BeforeEach
    void setUp() {
        deleteCurrencyCommand = new DeleteCurrencyCommand();
        deleteCurrencyCommand.userService = userService;
        deleteCurrencyCommand.telegramBot = telegramBot;
        deleteCurrencyCommand.messageService = messageService;
        deleteCurrencyCommand.outputCurrencies = Arrays.asList("USD", "EUR", "GBP");
    }

    @Test
    void getName_ShouldReturnDeleteCurrency() {
        // When
        String result = deleteCurrencyCommand.getName();

        // Then
        assertEquals("deletecurrency", result);
    }

    @Test
    void execute_ShouldSendNoneMessage_WhenUserHasNoOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("delete.currency.none")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(deleteCurrencyCommand.outputCurrencies, "to"))
                    .thenReturn(keyboard);

            // When
            deleteCurrencyCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.none");
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void execute_ShouldSendSelectMessage_WhenUserHasOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        List<String> userCurrencies = Arrays.asList("USD", "EUR");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(userCurrencies);
        when(messageService.createResponse("delete.currency.select")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(userCurrencies, "deletecurrency"))
                    .thenReturn(keyboard);

            // When
            deleteCurrencyCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.select");
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        TelegramApiException telegramApiException = new TelegramApiException("API Error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("delete.currency.none")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(telegramApiException);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(deleteCurrencyCommand.outputCurrencies, "to"))
                    .thenReturn(keyboard);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> deleteCurrencyCommand.execute(message, args));
            assertEquals(telegramApiException, exception.getCause());
        }
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenUserServiceThrowsException() {
        // Given
        Long chatId = 12345L;
        String args = "";
        RuntimeException userServiceException = new RuntimeException("User service error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenThrow(userServiceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deleteCurrencyCommand.execute(message, args));
        assertEquals(userServiceException, exception);
    }

    @Test
    void handleCallback_ShouldDeleteCurrencyAndSendSuccessMessage_WhenCurrencyExists() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "USD";
        Long chatId = 12345L;
        String emoji = "🇺🇸";
        List<String> userCurrencies = new ArrayList<>(Arrays.asList("USD", "EUR"));
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(userCurrencies);
        when(messageService.createResponse("delete.currency.success", emoji, data)).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenReturn(null);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);
        when(userService.update(user)).thenReturn(user);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getEmojiFlag(data)).thenReturn(emoji);

            // When
            deleteCurrencyCommand.handleCallback(callbackQuery, data);

            // Then
            verify(telegramBot).execute(any(AnswerCallbackQuery.class));
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.success", emoji, data);
            verify(userService).update(user);
            verify(telegramBot).execute(sendMessage);
            assertFalse(userCurrencies.contains("USD"));
            assertTrue(userCurrencies.contains("EUR"));
        }
    }

    @Test
    void handleCallback_ShouldNotUpdateUser_WhenCurrencyDoesNotExist() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "JPY";
        Long chatId = 12345L;
        String emoji = "🇯🇵";
        List<String> userCurrencies = new ArrayList<>(Arrays.asList("USD", "EUR"));
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(userCurrencies);
        when(messageService.createResponse("delete.currency.success", emoji, data)).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenReturn(null);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getEmojiFlag(data)).thenReturn(emoji);

            // When
            deleteCurrencyCommand.handleCallback(callbackQuery, data);

            // Then
            verify(telegramBot).execute(any(AnswerCallbackQuery.class));
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.success", emoji, data);
            verify(userService, never()).update(user);
            verify(telegramBot).execute(sendMessage);
            assertTrue(userCurrencies.contains("USD"));
            assertTrue(userCurrencies.contains("EUR"));
        }
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "USD";
        TelegramApiException telegramApiException = new TelegramApiException("API Error");
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenThrow(telegramApiException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deleteCurrencyCommand.handleCallback(callbackQuery, data));
        assertEquals(telegramApiException, exception.getCause());
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenUserServiceThrowsException() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "USD";
        Long chatId = 12345L;
        RuntimeException userServiceException = new RuntimeException("User service error");
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenReturn(null);
        when(userService.findOne(chatId)).thenThrow(userServiceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deleteCurrencyCommand.handleCallback(callbackQuery, data));
        assertEquals(userServiceException, exception);
    }

    @Test
    void execute_ShouldHandleNullArgs() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = null;
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("delete.currency.none")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(deleteCurrencyCommand.outputCurrencies, "to"))
                    .thenReturn(keyboard);

            // When
            deleteCurrencyCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.none");
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void execute_ShouldWorkWithDifferentChatIds() throws TelegramApiException {
        // Given
        Long chatId = 98765L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("delete.currency.none")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(deleteCurrencyCommand.outputCurrencies, "to"))
                    .thenReturn(keyboard);

            // When
            deleteCurrencyCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.none");
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void handleCallback_ShouldWorkWithDifferentCurrencies() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "EUR";
        Long chatId = 12345L;
        String emoji = "🇪🇺";
        List<String> userCurrencies = new ArrayList<>(Arrays.asList("USD", "EUR", "GBP"));
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(userCurrencies);
        when(messageService.createResponse("delete.currency.success", emoji, data)).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenReturn(null);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);
        when(userService.update(user)).thenReturn(user);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getEmojiFlag(data)).thenReturn(emoji);

            // When
            deleteCurrencyCommand.handleCallback(callbackQuery, data);

            // Then
            verify(telegramBot).execute(any(AnswerCallbackQuery.class));
            verify(userService).findOne(chatId);
            verify(messageService).createResponse("delete.currency.success", emoji, data);
            verify(userService).update(user);
            verify(telegramBot).execute(sendMessage);
            assertTrue(userCurrencies.contains("USD"));
            assertFalse(userCurrencies.contains("EUR"));
            assertTrue(userCurrencies.contains("GBP"));
        }
    }
}
