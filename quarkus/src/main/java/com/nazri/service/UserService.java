package com.nazri.service;

import com.nazri.model.User;
import com.nazri.repository.UserRepository;
import com.nazri.util.TimeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    public User create(Chat chat){
        log.info("Creating New User");
        User user = new User();
        user.setChatId(user.getChatId());
        user.setTelegramUsername(chat.getUserName());
        user.setCreatedDate(TimeUtil.getCurrentTime());
        user.setUpdatedDate(TimeUtil.getCurrentTime());
        return userRepository.create(user);
    }

    public User findOne(final long chatId){
        return userRepository.findOne(chatId);
    }

}