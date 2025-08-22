package com.nazri.command;

import com.nazri.model.User;
import com.nazri.service.MessageService;
import com.nazri.service.TelegramBot;
import com.nazri.service.TelegramResponse;
import com.nazri.service.UserService;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ApplicationScoped
public class GetCurrenciesCommand implements Command {

    private static final Logger log = Logger.getLogger(GetCurrenciesCommand.class);

    @Inject
    UserService userService;

    @Inject
    TelegramBot telegramBot;

    @Inject
    MessageService messageService;

    @Override
    public String getName() {
        return "getcurrencies";
    }

    @Override
    public void execute(Message message, String args) {
        User user = userService.findOne(message.getChatId());
        
        try {
            TelegramResponse response;
            
            if (user.getInputCurrency() == null && user.getOutputCurrency().isEmpty()) {
                response = messageService.createResponse("getcurrencies.empty");
            } else if (user.getInputCurrency() == null) {
                String outputList = formatOutputCurrencies(user.getOutputCurrency());
                response = messageService.createResponse("getcurrencies.no.input", outputList);
            } else if (user.getOutputCurrency().isEmpty()) {
                response = messageService.createResponse("getcurrencies.no.output", 
                        Util.getEmojiFlag(user.getInputCurrency()), user.getInputCurrency());
            } else {
                String outputList = formatOutputCurrencies(user.getOutputCurrency());
                response = messageService.createResponse("getcurrencies.complete", 
                        Util.getEmojiFlag(user.getInputCurrency()), user.getInputCurrency(), outputList);
            }
            
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String formatOutputCurrencies(List<String> currencies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currencies.size(); i++) {
            sb.append(i + 1)
              .append(". ")
              .append(Util.getEmojiFlag(currencies.get(i)))
              .append(" *")
              .append(currencies.get(i))
              .append("*\n");
        }
        return sb.toString();
    }

}
