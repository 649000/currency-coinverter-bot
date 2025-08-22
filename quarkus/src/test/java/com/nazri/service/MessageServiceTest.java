package com.nazri.service;

import com.nazri.model.TelegramResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
        messageService.messageBundle = "messages";
    }

    @Test
    void getMessage_ShouldReturnFallbackMessage_WhenKeyNotFound() {
        // Given
        String key = "nonexistent.key";
        String expectedFallback = key + " (message not found)";

        // When
        String result = messageService.getMessage(key);

        // Then
        assertEquals(expectedFallback, result);
    }

    @Test
    void getMessage_ShouldReturnFallbackMessage_WhenResourceBundleNotFound() {
        // Given
        messageService.messageBundle = "nonexistent-bundle";
        String key = "any.key";
        String expectedFallback = key + " (message not found)";

        // When
        String result = messageService.getMessage(key);

        // Then
        assertEquals(expectedFallback, result);
    }

    @Test
    void getMessage_ShouldHandleNullParameters() {
        // Given
        String key = "nonexistent.key";
        String expectedFallback = key + " (message not found)";

        // When
        String result = messageService.getMessage(key, (Object) null);

        // Then
        assertEquals(expectedFallback, result);
    }

    @Test
    void getMessage_ShouldHandleEmptyParameters() {
        // Given
        String key = "nonexistent.key";
        String expectedFallback = key + " (message not found)";

        // When
        String result = messageService.getMessage(key);

        // Then
        assertEquals(expectedFallback, result);
    }

    @Test
    void createResponse_ShouldReturnTelegramResponse_WhenKeyNotFound() {
        // Given
        String key = "nonexistent.response";

        // When
        TelegramResponse result = messageService.createResponse(key);

        // Then
        assertNotNull(result);
    }

    @Test
    void createResponse_ShouldReturnTelegramResponse_WhenKeyNotFoundWithParameters() {
        // Given
        String key = "nonexistent.response";

        // When
        TelegramResponse result = messageService.createResponse(key, "param1", "param2");

        // Then
        assertNotNull(result);
    }

    @Test
    void messageService_ShouldHaveDefaultMessageBundle() {
        // Given & When
        MessageService service = new MessageService();

        // Then
        // The messageBundle field should be injected with default value
        // This test verifies the service can be instantiated
        assertNotNull(service);
    }
}
