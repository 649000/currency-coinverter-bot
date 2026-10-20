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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FromCurrencyCommandTest {

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

    private FromCurrencyCommand fromCurrencyCommand;

    private final List<String> inputCurrencies = Arrays.asList("USD", "EUR", "GBP", "SGD");

    @BeforeEach
    void setUp() {
        fromCurrencyCommand = new FromCurrencyCommand();
        fromCurrencyCommand.userService = userService;
        fromCurrencyCommand.telegramBot = telegramBot;
        fromCurrencyCommand.currencyService = currencyService;
        fromCurrencyCommand.messageService = messageService;
        fromCurrencyCommand.inputCurrencies = inputCurrencies;
    }

    @Test
    void getName_ShouldReturnFrom() {
        assertEquals("from", fromCurrencyCommand.getName());
    }

    @Test
    void execute_ShouldSendEmptyMessage_WhenArgsIsNull() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode(null)).thenReturn(null);
        when(messageService.createResponse("from.currency.empty")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        fromCurrencyCommand.execute(message, null);

        verify(messageService).createResponse("from.currency.empty");
        verify(telegramResponse).keyboard(any(InlineKeyboardMarkup.class));
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldSendEmptyMessage_WhenArgsIsEmpty() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("")).thenReturn(null);
        when(messageService.createResponse("from.currency.empty")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        fromCurrencyCommand.execute(message, "");

        verify(messageService).createResponse("from.currency.empty");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldSendInvalidMessage_WhenCurrencyCodeNotFound() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("INVALID")).thenReturn(null);
        when(messageService.createResponse("from.currency.invalid")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        fromCurrencyCommand.execute(message, "INVALID");

        verify(messageService).createResponse("from.currency.invalid");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
    }

    @Test
    void execute_ShouldUpdateUserAndSendConfirmation_WhenValidCurrencyProvided() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("USD")).thenReturn("USD");
        when(userService.findOne(chatId)).thenReturn(user);
        when(messageService.createResponse(eq("from.currency.set"), any(), eq("USD"))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        fromCurrencyCommand.execute(message, "USD");

        verify(userService).findOne(chatId);
        verify(user).setInputCurrency("USD");
        verify(userService).update(user);
        verify(messageService).createResponse(eq("from.currency.set"), any(), eq("USD"));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode("USD")).thenReturn("USD");
        when(userService.findOne(chatId)).thenReturn(user);
        when(messageService.createResponse(eq("from.currency.set"), any(), eq("USD"))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(new TelegramApiException("Send error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                fromCurrencyCommand.execute(message, "USD"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }

    @Test
    void handleCallback_ShouldAnswerCallbackQueryAndExecuteCommand() throws TelegramApiException {
        Long chatId = 12345L;
        String callbackId = "cb123";
        String currency = "EUR";

        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);
        when(currencyService.getCurrencyCode(currency)).thenReturn("EUR");
        when(userService.findOne(chatId)).thenReturn(user);
        when(messageService.createResponse(eq("from.currency.set"), any(), eq("EUR"))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        fromCurrencyCommand.handleCallback(callbackQuery, currency);

        verify(telegramBot).execute(any(AnswerCallbackQuery.class));
        verify(user).setInputCurrency("EUR");
        verify(userService).update(user);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenAnswerCallbackQueryFails() throws TelegramApiException {
        when(callbackQuery.getId()).thenReturn("cb123");
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenThrow(new TelegramApiException("Callback failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                fromCurrencyCommand.handleCallback(callbackQuery, "EUR"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }
}
