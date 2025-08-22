package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.model.User;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import com.nazri.util.KeyboardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrenciesCommandTest {

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

    private GetCurrenciesCommand getCurrenciesCommand;

    @BeforeEach
    void setUp() {
        getCurrenciesCommand = new GetCurrenciesCommand();
        getCurrenciesCommand.userService = userService;
        getCurrenciesCommand.telegramBot = telegramBot;
        getCurrenciesCommand.messageService = messageService;
        getCurrenciesCommand.outputCurrencies = Arrays.asList("USD", "EUR", "GBP");
        getCurrenciesCommand.inputCurrencies = Arrays.asList("SGD", "MYR", "JPY");
    }

    @Test
    void getName_ShouldReturnGetCurrencies() {
        // When
        String result = getCurrenciesCommand.getName();

        // Then
        assertEquals("getcurrencies", result);
    }

    @Test
    void execute_ShouldSendEmptyMessage_WhenUserHasNoInputAndNoOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("getcurrencies.empty")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        getCurrenciesCommand.execute(message, args);

        // Then
        verify(userService).findOne(chatId);
        verify(messageService).createResponse("getcurrencies.empty");
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldSendNoInputMessage_WhenUserHasNoInputButHasOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        List<String> outputCurrencies = Arrays.asList("USD", "EUR");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(outputCurrencies);
        when(messageService.createResponse(eq("getcurrencies.no.input"), anyString())).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(getCurrenciesCommand.inputCurrencies, "from"))
                    .thenReturn(keyboard);

            // When
            getCurrenciesCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse(eq("getcurrencies.no.input"), anyString());
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void execute_ShouldSendNoOutputMessage_WhenUserHasInputButNoOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        String inputCurrency = "SGD";
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(inputCurrency);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse(eq("getcurrencies.no.output"), anyString(), eq(inputCurrency))).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        try (MockedStatic<KeyboardUtil> keyboardUtilMock = mockStatic(KeyboardUtil.class)) {
            keyboardUtilMock.when(() -> KeyboardUtil.createCurrencyKeyboard(getCurrenciesCommand.outputCurrencies, "to"))
                    .thenReturn(keyboard);

            // When
            getCurrenciesCommand.execute(message, args);

            // Then
            verify(userService).findOne(chatId);
            verify(messageService).createResponse(eq("getcurrencies.no.output"), anyString(), eq(inputCurrency));
            verify(telegramResponse).keyboard(keyboard);
            verify(telegramResponse).toMessage(chatId);
            verify(telegramBot).execute(sendMessage);
        }
    }

    @Test
    void execute_ShouldSendCompleteMessage_WhenUserHasBothInputAndOutputCurrencies() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        String inputCurrency = "SGD";
        List<String> outputCurrencies = Arrays.asList("USD", "EUR");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(inputCurrency);
        when(user.getOutputCurrency()).thenReturn(outputCurrencies);
        when(messageService.createResponse(eq("getcurrencies.complete"), anyString(), eq(inputCurrency), anyString())).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        getCurrenciesCommand.execute(message, args);

        // Then
        verify(userService).findOne(chatId);
        verify(messageService).createResponse(eq("getcurrencies.complete"), anyString(), eq(inputCurrency), anyString());
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        TelegramApiException telegramApiException = new TelegramApiException("API Error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("getcurrencies.empty")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(telegramApiException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getCurrenciesCommand.execute(message, args));
        assertEquals(telegramApiException, exception.getCause());
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
                () -> getCurrenciesCommand.execute(message, args));
        assertEquals(userServiceException, exception);
    }

    @Test
    void handleCallback_ShouldAnswerCallbackAndExecuteCommand() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "test-data";
        Long chatId = 12345L;
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("getcurrencies.empty")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenReturn(null);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        getCurrenciesCommand.handleCallback(callbackQuery, data);

        // Then
        verify(telegramBot).execute(any(AnswerCallbackQuery.class));
        verify(userService).findOne(chatId);
        verify(messageService).createResponse("getcurrencies.empty");
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        // Given
        String callbackId = "callback123";
        String data = "test-data";
        TelegramApiException telegramApiException = new TelegramApiException("API Error");
        
        when(callbackQuery.getId()).thenReturn(callbackId);
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenThrow(telegramApiException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getCurrenciesCommand.handleCallback(callbackQuery, data));
        assertEquals(telegramApiException, exception.getCause());
    }

    @Test
    void formatOutputCurrencies_ShouldFormatCorrectly() throws Exception {
        // Given
        List<String> currencies = Arrays.asList("USD", "EUR", "GBP");
        
        // Use reflection to access the private method
        java.lang.reflect.Method method = GetCurrenciesCommand.class.getDeclaredMethod("formatOutputCurrencies", List.class);
        method.setAccessible(true);

        // When
        String result = (String) method.invoke(getCurrenciesCommand, currencies);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("1."));
        assertTrue(result.contains("2."));
        assertTrue(result.contains("3."));
        assertTrue(result.contains("USD"));
        assertTrue(result.contains("EUR"));
        assertTrue(result.contains("GBP"));
    }

    @Test
    void execute_ShouldHandleNullArgs() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = null;
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("getcurrencies.empty")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        getCurrenciesCommand.execute(message, args);

        // Then
        verify(userService).findOne(chatId);
        verify(messageService).createResponse("getcurrencies.empty");
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldWorkWithDifferentChatIds() throws TelegramApiException {
        // Given
        Long chatId = 98765L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("getcurrencies.empty")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        getCurrenciesCommand.execute(message, args);

        // Then
        verify(userService).findOne(chatId);
        verify(messageService).createResponse("getcurrencies.empty");
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }
}
