package com.nazri.service;

import com.nazri.model.TelegramResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@ApplicationScoped
public class MessageService {

    @ConfigProperty(name = "app.messages.bundle", defaultValue = "messages")
    String messageBundle;

    /**
     * Get a formatted message from the resource bundle
     *
     * @param key the message key
     * @param params parameters to format into the message
     * @return formatted message
     */
    public String getMessage(String key, Object... params) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(messageBundle);
            String template = bundle.getString(key);
            return MessageFormat.format(template, params);
        } catch (Exception e) {
            // Fallback to key if message not found
            return key + " (message not found)";
        }
    }

    /**
     * Create a TelegramResponse with a message from the bundle
     *
     * @param messageKey the message key
     * @param params parameters to format into the message
     * @return TelegramResponse builder
     */
    public TelegramResponse createResponse(String messageKey, Object... params) {
        return TelegramResponse.builder()
                .text(getMessage(messageKey, params));
    }
}
