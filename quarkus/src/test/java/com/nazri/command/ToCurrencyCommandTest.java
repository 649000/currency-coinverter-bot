package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.model.User;
import com.nazri.service.CurrencyService;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToCurrencyCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private CurrencyService currencyService;

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
    private CallbackQuery callbackQuery;

    @Mock
    private Message callbackMessage;

    private ToCurrencyCommand toCurrencyCommand;

    private final List<String> outputCurrencies = Arrays.asList("MYR", "SGD", "EUR");

    @BeforeEach
    void setUp() {
        toCurrencyCommand = new ToCurrencyCommand();
        toCurrencyCommand.userService = userService;
        toCurrencyCommand.telegramBot = telegramBot;
        toCurrencyCommand.currencyService = currencyService;
        toCurrencyCommand.messageService = messageService;
        toCurrencyCommand.outputCurrencies = outputCurrencies;
    }

    @Test
    void getName_ShouldReturnTo() {
        assertEquals("to", toCurrencyCommand.getName());
    }

    @Test
    void execute_ShouldSendEmptyMessage_WhenArgsIsNull() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode(null)).thenReturn(null);
        when(messageService.createResponse("to.currency.empty")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.execute(message, null);

        verify(messageService).createResponse("to.currency.empty");
        verify(telegramResponse).keyboard(any(InlineKeyboardMarkup.class));
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldSendEmptyMessage_WhenArgsIsEmpty() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("")).thenReturn(null);
        when(messageService.createResponse("to.currency.empty")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.execute(message, "");

        verify(messageService).createResponse("to.currency.empty");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldSendInvalidMessage_WhenCurrencyCodeNotFound() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("UNKNOWN")).thenReturn(null);
        when(messageService.createResponse("to.currency.invalid")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.execute(message, "UNKNOWN");

        verify(messageService).createResponse("to.currency.invalid");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldSendLimitMessage_WhenUserAlreadyHas3OrMoreOutputCurrencies() throws TelegramApiException {
        Long chatId = 12345L;
        List<String> currentOutput = new ArrayList<>(Arrays.asList("USD", "SGD", "JPY"));

        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("MYR")).thenReturn("MYR");
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(currentOutput);
        when(messageService.createResponse("to.currency.limit")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.execute(message, "MYR");

        verify(userService).findOne(chatId);
        verify(messageService).createResponse("to.currency.limit");
        verify(telegramResponse).keyboard(any(InlineKeyboardMarkup.class));
        verify(telegramBot).execute(sendMessage);
        verify(userService, never()).update(any());
    }

    @Test
    void execute_ShouldAddCurrencyAndUpdateUser_WhenUnderLimit() throws TelegramApiException {
        Long chatId = 12345L;
        List<String> currentOutput = new ArrayList<>(Arrays.asList("USD"));

        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("MYR")).thenReturn("MYR");
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(currentOutput);
        when(messageService.createResponse(eq("to.currency.set"), any(), eq("MYR"), anyString())).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.execute(message, "MYR");

        verify(userService).findOne(chatId);
        assertTrue(currentOutput.contains("MYR"));
        verify(userService).update(user);
        verify(messageService).createResponse(eq("to.currency.set"), any(), eq("MYR"), anyString());
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("MYR")).thenReturn("MYR");
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(new ArrayList<>());
        when(messageService.createResponse(eq("to.currency.set"), any(), eq("MYR"), anyString())).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(new TelegramApiException("Telegram error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                toCurrencyCommand.execute(message, "MYR"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }

    @Test
    void handleCallback_ShouldAnswerCallbackQueryAndExecuteCommand() throws TelegramApiException {
        Long chatId = 12345L;
        String callbackId = "cb456";
        String currency = "JPY";
        List<String> currentOutput = new ArrayList<>();

        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode(currency)).thenReturn("JPY");
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getOutputCurrency()).thenReturn(currentOutput);
        when(messageService.createResponse(eq("to.currency.set"), any(), eq("JPY"), anyString())).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        toCurrencyCommand.handleCallback(callbackQuery, currency);

        verify(telegramBot).execute(any(AnswerCallbackQuery.class));
        assertTrue(currentOutput.contains("JPY"));
        verify(userService).update(user);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenAnswerCallbackQueryFails() throws TelegramApiException {
        when(callbackQuery.getId()).thenReturn("cb456");
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenThrow(new TelegramApiException("Callback failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                toCurrencyCommand.handleCallback(callbackQuery, "JPY"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }
}
