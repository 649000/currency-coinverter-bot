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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConvertCommandTest {

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
    private Chat chat;

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

    private ConvertCommand convertCommand;

    private final List<String> commonAmount = Arrays.asList("10", "50", "100", "500");
    private final List<String> outputCurrencies = Arrays.asList("MYR", "SGD", "EUR");
    private final List<String> inputCurrencies = Arrays.asList("USD", "EUR", "GBP");
    private final List<String> multiplierList = Arrays.asList("1", "10", "100");
    private final List<String> multiplierSymbols = Arrays.asList("1x", "10x", "100x");

    @BeforeEach
    void setUp() {
        convertCommand = new ConvertCommand();
        convertCommand.userService = userService;
        convertCommand.telegramBot = telegramBot;
        convertCommand.currencyService = currencyService;
        convertCommand.messageService = messageService;
        convertCommand.commonAmount = commonAmount;
        convertCommand.outputCurrencies = outputCurrencies;
        convertCommand.inputCurrencies = inputCurrencies;
        convertCommand.multiplierList = multiplierList;
        convertCommand.multiplierSymbols = multiplierSymbols;
    }

    @Test
    void getName_ShouldReturnConvert() {
        assertEquals("convert", convertCommand.getName());
    }

    @Test
    void execute_ShouldSendInvalidNumericMessage_WhenArgsNotNumeric() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("convert.invalid.numeric")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        convertCommand.execute(message, "not-a-number");

        verify(messageService).createResponse("convert.invalid.numeric");
        verify(telegramResponse).keyboard(any(InlineKeyboardMarkup.class));
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(userService);
        verifyNoInteractions(currencyService);
    }

    @Test
    void execute_ShouldSendMissingInputCurrency_WhenUserHasNoInputCurrency() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn(null);
        when(messageService.createResponse("convert.missing.input.currency")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        convertCommand.execute(message, "100");

        verify(userService).findOne(chatId);
        verify(messageService).createResponse("convert.missing.input.currency");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(currencyService);
    }

    @Test
    void execute_ShouldSendMissingOutputCurrency_WhenUserHasEmptyOutputCurrencies() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn("USD");
        when(user.getOutputCurrency()).thenReturn(Collections.emptyList());
        when(messageService.createResponse("convert.missing.output.currency")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        convertCommand.execute(message, "100");

        verify(userService).findOne(chatId);
        verify(messageService).createResponse("convert.missing.output.currency");
        verify(telegramBot).execute(sendMessage);
        verifyNoInteractions(currencyService);
    }

    @Test
    void execute_ShouldConvertSuccessfully_WhenValidNumericAndCurrenciesPresent() throws TelegramApiException {
        Long chatId = 12345L;
        List<String> userOutputCurrencies = Arrays.asList("MYR", "SGD");

        when(message.getChatId()).thenReturn(chatId);
        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn("USD");
        when(user.getOutputCurrency()).thenReturn(userOutputCurrencies);

        Map<String, BigDecimal> conversionResults = new HashMap<>();
        conversionResults.put("MYR", new BigDecimal("445.00"));
        conversionResults.put("SGD", new BigDecimal("135.00"));
        when(currencyService.convertCurrency(eq(new BigDecimal("100.0")), eq("USD"), eq(userOutputCurrencies)))
                .thenReturn(conversionResults);

        when(messageService.createResponse(eq("convert.result"), any(), any())).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        convertCommand.execute(message, "100");

        verify(currencyService).convertCurrency(eq(new BigDecimal("100.0")), eq("USD"), eq(userOutputCurrencies));
        verify(messageService).createResponse(eq("convert.result"), any(), any());
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionThrown() throws TelegramApiException {
        Long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("convert.invalid.numeric")).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(new TelegramApiException("Telegram error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                convertCommand.execute(message, "invalid"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }

    @Test
    void handleCallback_ShouldAnswerCallbackQueryAndExecuteCommand() throws TelegramApiException {
        Long chatId = 12345L;
        String callbackId = "cb123";
        String callbackData = "50";

        when(callbackQuery.getId()).thenReturn(callbackId);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackMessage.getChatId()).thenReturn(chatId);

        when(userService.findOne(chatId)).thenReturn(user);
        when(user.getInputCurrency()).thenReturn("USD");
        when(user.getOutputCurrency()).thenReturn(Collections.singletonList("EUR"));

        Map<String, BigDecimal> conversionResults = Collections.singletonMap("EUR", new BigDecimal("46.00"));
        when(currencyService.convertCurrency(eq(new BigDecimal("50.0")), eq("USD"), any()))
                .thenReturn(conversionResults);

        when(messageService.createResponse(eq("convert.result"), any(), any())).thenReturn(telegramResponse);
        when(telegramResponse.keyboard(any(InlineKeyboardMarkup.class))).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);

        convertCommand.handleCallback(callbackQuery, callbackData);

        verify(telegramBot).execute(any(AnswerCallbackQuery.class));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void handleCallback_ShouldThrowRuntimeException_WhenAnswerCallbackQueryFails() throws TelegramApiException {
        when(callbackQuery.getId()).thenReturn("cb123");
        when(telegramBot.execute(any(AnswerCallbackQuery.class))).thenThrow(new TelegramApiException("Failed callback"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                convertCommand.handleCallback(callbackQuery, "100"));

        assertInstanceOf(TelegramApiException.class, exception.getCause());
    }
}
