package com.nazri.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.nazri.service.WebhookService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/api/telegram")
public class WebhookController {

    private static final Logger log = Logger.getLogger(WebhookController.class);

    @Inject
    WebhookService webhookService;

    @POST
    @Path("/webhook")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updates(@Context APIGatewayV2HTTPEvent event) {
        return Response.ok()
                .entity("Update processed successfully")
                .build();
    }
}
