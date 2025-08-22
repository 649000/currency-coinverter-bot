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
            StringBuilder inputCurrency = new StringBuilder();
            if (user.getInputCurrency() == null) {
                inputCurrency.append("\n\n");
            } else {
                inputCurrency.append("1. ")
                        .append(Util.getEmojiFlag(user.getInputCurrency()))
                        .append(" *")
                        .append(user.getInputCurrency())
                        .append("*\n\n");
            }

            StringBuilder outputCurrencies = new StringBuilder();
            for (int i = 0; i < user.getOutputCurrency().size(); i++) {
                outputCurrencies.append(i + 1)
                        .append(". ")
                        .append(Util.getEmojiFlag(user.getOutputCurrency().get(i)))
                        .append(" *")
                        .append(user.getOutputCurrency().get(i))
                        .append("* \n");
            }

            String helpText = "";
            if (user.getInputCurrency() == null || user.getOutputCurrency().isEmpty()) {
                helpText = "\n\nUse /help to know more on setting your input/output currencies";
            }

            TelegramResponse response = messageService.createResponse("getcurrencies.display", 
                    inputCurrency.toString(), outputCurrencies.toString(), helpText);
            
            telegramBot.execute(response.toMessage(message.getChatId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
