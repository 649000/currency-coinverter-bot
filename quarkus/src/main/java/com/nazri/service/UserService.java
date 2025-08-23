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

/**
 * Service class for handling user-related operations.
 * 
 * This service provides functionality for creating, finding, and updating users
 * in the system. It manages user data persistence through the UserRepository
 * and ensures data integrity by removing duplicate output currencies.
 */
@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    /**
     * Creates a new user based on Telegram chat information.
     * 
     * This method initializes a new user with default settings:
     * - Sets the chat ID from the Telegram chat
     * - Initializes output currency list with SGD
     * - Sets the Telegram username if available
     * - Sets creation and update timestamps
     * 
     * @param chat The Telegram Chat object containing user information
     * @return The newly created User object
     */
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

    /**
     * Finds a user by their chat ID.
     * 
     * Retrieves user information from the database using the Telegram chat ID
     * as the primary key. Returns null if no user is found with the given ID.
     * 
     * @param chatId The Telegram chat ID of the user to find
     * @return The User object if found, null otherwise
     */
    public User findOne(final long chatId) {
        return userRepository.findOne(chatId);
    }

    /**
     * Updates an existing user's information.
     * 
     * This method ensures data integrity by removing duplicate output currencies
     * from the user's list before persisting the changes. It also updates the
     * last modified timestamp.
     * 
     * @param user The User object to update
     * @return The updated User object
     */
    public User update(final User user) {
        // Remove duplicates from output currency list to ensure data integrity
        user.setOutputCurrency(user.getOutputCurrency().stream().distinct().collect(Collectors.toList()));
        log.info("Updating User: " + user.toString());
        return userRepository.update(user);
    }

}
