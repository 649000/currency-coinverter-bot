package com.nazri.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.nazri.service.TelegramBot;
import com.nazri.service.CurrencyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;

@ApplicationScoped
@Path("/api/telegram")
public class WebhookController {

    private static final Logger log = Logger.getLogger(WebhookController.class);

    @Inject
    CurrencyService currencyService;

    @Inject
    TelegramBot telegramBot;


    @POST
    @Path("/webhook")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updates(Update update, @Context APIGatewayV2HTTPEvent event) {
        log.info("APIGatewayV2HTTPEvent: " + event.toString());
        telegramBot.onWebhookUpdateReceived(update);
        return Response.ok()
                .entity("Update processed successfully")
                .build();
    }

    @GET
    @Path("/health")
    public String healthCheck() {
        try {
            currencyService.fetchCurrencies();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

}
