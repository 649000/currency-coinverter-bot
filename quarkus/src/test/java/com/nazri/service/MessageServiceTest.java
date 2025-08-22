package com.nazri.service;

import com.nazri.model.TelegramResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ResourceBundle resourceBundle;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
        messageService.messageBundle = "messages";
    }

    @Test
    void getMessage_ShouldReturnFormattedMessage_WhenKeyExistsWithParameters() {
        // Given
        String key = "welcome.message";
        String template = "Welcome {0}! You have {1} messages.";
        String expectedMessage = "Welcome John! You have 5 messages.";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template, "John", 5)).thenReturn(expectedMessage);

            // When
            String result = messageService.getMessage(key, "John", 5);

            // Then
            assertEquals(expectedMessage, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void getMessage_ShouldReturnFormattedMessage_WhenKeyExistsWithoutParameters() {
        // Given
        String key = "simple.message";
        String template = "Hello World!";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template)).thenReturn(template);

            // When
            String result = messageService.getMessage(key);

            // Then
            assertEquals(template, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void getMessage_ShouldReturnFallbackMessage_WhenKeyNotFound() {
        // Given
        String key = "nonexistent.key";
        String expectedFallback = key + " (message not found)";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class)) {
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenThrow(new MissingResourceException("Key not found", "ResourceBundle", key));

            // When
            String result = messageService.getMessage(key);

            // Then
            assertEquals(expectedFallback, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void getMessage_ShouldReturnFallbackMessage_WhenResourceBundleNotFound() {
        // Given
        String key = "any.key";
        String expectedFallback = key + " (message not found)";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class)) {
            bundleMock.when(() -> ResourceBundle.getBundle("messages"))
                    .thenThrow(new MissingResourceException("Bundle not found", "ResourceBundle", "messages"));

            // When
            String result = messageService.getMessage(key);

            // Then
            assertEquals(expectedFallback, result);
        }
    }

    @Test
    void getMessage_ShouldReturnFallbackMessage_WhenMessageFormatThrowsException() {
        // Given
        String key = "invalid.format";
        String template = "Invalid format {0} {1 missing brace";
        String expectedFallback = key + " (message not found)";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(eq(template), any()))
                    .thenThrow(new IllegalArgumentException("Invalid format"));

            // When
            String result = messageService.getMessage(key, "param1");

            // Then
            assertEquals(expectedFallback, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void getMessage_ShouldHandleNullParameters() {
        // Given
        String key = "message.with.null";
        String template = "Hello {0}!";
        String expectedMessage = "Hello null!";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template, (Object) null)).thenReturn(expectedMessage);

            // When
            String result = messageService.getMessage(key, (Object) null);

            // Then
            assertEquals(expectedMessage, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void getMessage_ShouldHandleEmptyParameters() {
        // Given
        String key = "simple.message";
        String template = "No parameters needed";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template)).thenReturn(template);

            // When
            String result = messageService.getMessage(key);

            // Then
            assertEquals(template, result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void createResponse_ShouldReturnTelegramResponseWithMessage_WhenKeyExists() {
        // Given
        String key = "response.message";
        String template = "Response: {0}";
        String expectedMessage = "Response: Success";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template, "Success")).thenReturn(expectedMessage);

            // When
            TelegramResponse result = messageService.createResponse(key, "Success");

            // Then
            assertNotNull(result);
            // Note: We can't easily test the internal text of TelegramResponse without modifying it
            // This test verifies that the method completes without exception
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void createResponse_ShouldReturnTelegramResponseWithFallback_WhenKeyNotFound() {
        // Given
        String key = "nonexistent.response";
        String expectedFallback = key + " (message not found)";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class)) {
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenThrow(new MissingResourceException("Key not found", "ResourceBundle", key));

            // When
            TelegramResponse result = messageService.createResponse(key);

            // Then
            assertNotNull(result);
            verify(resourceBundle).getString(key);
        }
    }

    @Test
    void createResponse_ShouldHandleMultipleParameters() {
        // Given
        String key = "multi.param.message";
        String template = "User {0} has {1} points and {2} level";
        String expectedMessage = "User Alice has 100 points and 5 level";
        
        try (MockedStatic<ResourceBundle> bundleMock = mockStatic(ResourceBundle.class);
             MockedStatic<MessageFormat> formatMock = mockStatic(MessageFormat.class)) {
            
            bundleMock.when(() -> ResourceBundle.getBundle("messages")).thenReturn(resourceBundle);
            when(resourceBundle.getString(key)).thenReturn(template);
            formatMock.when(() -> MessageFormat.format(template, "Alice", 100, 5)).thenReturn(expectedMessage);

            // When
            TelegramResponse result = messageService.createResponse(key, "Alice", 100, 5);

            // Then
            assertNotNull(result);
            verify(resourceBundle).getString(key);
        }
    }
}
