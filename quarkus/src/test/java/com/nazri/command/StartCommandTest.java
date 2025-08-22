package com.nazri.command;

import com.nazri.model.TelegramResponse;
import com.nazri.model.User;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private TelegramResponse telegramResponse;

    @Mock
    private SendMessage sendMessage;

    @Mock
    private Message executedMessage;

    @Mock
    private User user;

    private StartCommand startCommand;

    @BeforeEach
    void setUp() {
        startCommand = new StartCommand();
        startCommand.telegramBot = telegramBot;
        startCommand.userService = userService;
        startCommand.messageService = messageService;
    }

    @Test
    void getName_ShouldReturnStart() {
        // When
        String result = startCommand.getName();

        // Then
        assertEquals("start", result);
    }

    @Test
    void execute_ShouldSendWelcomeMessageAndCreateUser_WhenUserDoesNotExist() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(message.getChat()).thenReturn(chat);
        when(messageService.createResponse("start.welcome")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);
        when(userService.findOne(chatId)).thenReturn(null);
        when(userService.create(chat)).thenReturn(user);

        // When
        startCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("start.welcome");
        verify(userService).findOne(chatId);
        verify(userService).create(chat);
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldHandleArgsParameter_WhenArgsProvided() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "some arguments";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("start.welcome")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);
        when(userService.findOne(chatId)).thenReturn(user);

        // When
        startCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("start.welcome");
        verify(userService).findOne(chatId);
        verify(telegramResponse).toMessage(chatId);
        verify(telegramBot).execute(sendMessage);
    }

    @Test
    void execute_ShouldHandleMessageServiceException_WhenCreateResponseFails() throws TelegramApiException {
        // Given
        String args = "";
        RuntimeException serviceException = new RuntimeException("Message service error");
        
        when(messageService.createResponse("start.welcome")).thenThrow(serviceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> startCommand.execute(message, args));
        assertEquals(serviceException, exception);
        
        verify(messageService).createResponse("start.welcome");
        verify(userService, never()).findOne(any(Long.class));
        verify(telegramResponse, never()).toMessage(any(Long.class));
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    void execute_ShouldHandleUserServiceException_WhenFindOneFails() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        RuntimeException userServiceException = new RuntimeException("User service error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("start.welcome")).thenReturn(telegramResponse);
        when(userService.findOne(chatId)).thenThrow(userServiceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> startCommand.execute(message, args));
        assertEquals(userServiceException, exception);
        
        verify(messageService).createResponse("start.welcome");
        verify(userService).findOne(chatId);
        verify(userService, never()).create(any(Chat.class));
        verify(telegramResponse, never()).toMessage(any(Long.class));
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    void execute_ShouldHandleUserServiceException_WhenCreateFails() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        RuntimeException userServiceException = new RuntimeException("User creation error");
        
        when(message.getChatId()).thenReturn(chatId);
        when(message.getChat()).thenReturn(chat);
        when(messageService.createResponse("start.welcome")).thenReturn(telegramResponse);
        when(userService.findOne(chatId)).thenReturn(null);
        when(userService.create(chat)).thenThrow(userServiceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> startCommand.execute(message, args));
        assertEquals(userServiceException, exception);
        
        verify(messageService).createResponse("start.welcome");
        verify(userService).findOne(chatId);
        verify(userService).create(chat);
        verify(telegramResponse, never()).toMessage(any(Long.class));
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    void execute_ShouldCallCorrectMessageKey() throws TelegramApiException {
        // Given
        Long chatId = 12345L;
        String args = "";
        
        when(message.getChatId()).thenReturn(chatId);
        when(messageService.createResponse("start.welcome")).thenReturn(telegramResponse);
        when(telegramResponse.toMessage(chatId)).thenReturn(sendMessage);
        when(telegramBot.execute(sendMessage)).thenReturn(executedMessage);
        when(userService.findOne(chatId)).thenReturn(user);

        // When
        startCommand.execute(message, args);

        // Then
        verify(messageService).createResponse("start.welcome");
        verify(messageService, never()).createResponse(argThat(key -> !key.equals("start.welcome")));
    }
}
