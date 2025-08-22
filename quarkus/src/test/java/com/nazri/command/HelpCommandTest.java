package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private MessageService messageService;

    @Mock
    private Message message;

    @Mock
    private TelegramResponse telegramResponse;

    @Mock
    private SendMessage sendMessage;

    @Mock
    private Message executedMessage;

    private HelpCommand helpCommand;

    @BeforeEach
    void setUp() {
        helpCommand = new HelpCommand();
        helpCommand.telegramBot = telegramBot;
        helpCommand.messageService = messageService;
    }

    @Test
    void getName_ShouldReturnHelp() {
        // When
        String result = helpCommand.getName();

        // Then
        assertEquals("help", result);
    }

    @Test
    void execute_ShouldSendHelpMessage_WhenSuccessful() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        helpCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldHandleArgsParameter_WhenArgsProvided() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "some arguments";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        helpCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldHandleNullArgs_WhenArgsIsNull() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = null;
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        helpCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldThrowRuntimeException_WhenTelegramApiExceptionOccurs() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        TelegramApiException telegramApiException = new TelegramApiException("API Error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenThrow(telegramApiException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> helpCommand.execute(message, args));
        assertEquals(telegramApiException, exception.getCause());
        
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldHandleMessageServiceException_WhenCreateResponseFails() {
        // Given
        Long chatId = 12345L;
        String args = "";
        RuntimeException serviceException = new RuntimeException("Message service error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenThrow(serviceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> helpCommand.execute(message, args));
        assertEquals(serviceException, exception);
        
        verify(messageService).createResponse("help.content");
        verify(telegramResponse, never()).toMessage(any());
        verify(telegramBot, never()).execute(any());
    }

    @Test
    void execute_ShouldHandleTelegramResponseException_WhenToMessageFails() {
        // Given
        Long chatId = 12345L;
        String args = "";
        RuntimeException responseException = new RuntimeException("Response conversion error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenThrow(responseException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> helpCommand.execute(message, args));
        assertEquals(responseException, exception);
        
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot, never()).execute(any());
    }

    @Test
    void execute_ShouldWorkWithDifferentChatIds() throws TelegramApiException {
        // Given
        Long chatId = 98765L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        helpCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("help.content");
        verify(telegramResponse).toMessage(String.valueOf(chatId));
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldCallCorrectMessageKey() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("help.content")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(String.valueOf(chatId))).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);

        // When
        helpCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("help.content");
        verify(messageService, never()).createResponse(argThat(key -> !key.equals("help.content")));
    }
}
