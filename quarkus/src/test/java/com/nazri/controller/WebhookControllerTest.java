package com.nazri.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.nazri.service.TelegramBot;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private Update update;

    @Mock
    private APIGatewayV2HTTPEvent event;

    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        webhookController = new WebhookController();
        webhookController.telegramBot = telegramBot;
    }

    @Test
    void updates_ShouldCallTelegramBotAndReturnOk_WhenEventAndUpdateProvided() {
        when(event.toString()).thenReturn("APIGatewayV2HTTPEvent{...}");

        Response response = webhookController.updates(update, event);

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Update processed successfully", response.getEntity());
        verify(telegramBot).onWebhookUpdateReceived(update);
    }

    @Test
    void updates_ShouldHandleNullEventSafely() {
        Response response = webhookController.updates(update, null);

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Update processed successfully", response.getEntity());
        verify(telegramBot).onWebhookUpdateReceived(update);
    }
}
