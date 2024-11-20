package com.nazri.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@ApplicationScoped
public class TelegramBotConfig {
    @ConfigProperty(name = "telegram.bot.token")
    String botToken;

    @Produces
    @ApplicationScoped
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }
}
