package com.nazri.service;

import com.nazri.model.User;
import com.nazri.repository.UserRepository;
import com.nazri.util.Util;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.Arrays;
import java.util.stream.Collectors;


@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    public User create(Chat chat) {
        log.info("Creating New User");
        User user = new User();
        user.setChatId(chat.getId());
        user.setOutputCurrency(Arrays.asList("SGD"));
        user.setTelegramUsername(chat.getUserName());
        user.setCreatedDate(Util.getCurrentTime());
        user.setUpdatedDate(Util.getCurrentTime());
        return userRepository.create(user);
    }

    public User findOne(final long chatId) {
        return userRepository.findOne(chatId);
    }

    public User update(final User user) {
        // Remove duplicates
        user.setOutputCurrency(user.getOutputCurrency().stream().distinct().collect(Collectors.toList()));
        log.info("Updating User: " + user.toString());
        return userRepository.update(user);
    }

}
