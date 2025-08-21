package com.nazri.repository;

import com.nazri.model.User;
import com.nazri.util.Constant;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@ApplicationScoped
public class UserRepository {

    private static final Logger log = Logger.getLogger(UserRepository.class);

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<User> userDynamoDbTable;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        try {
            this.userDynamoDbTable = dynamoDbEnhancedClient.table(Constant.USER_TABLE, TableSchema.fromBean(User.class));
            log.info("DynamoDB client and table configured successfully.");
        } catch (DynamoDbException e) {
            log.error("Error configuring DynamoDB table: " + e.getMessage(), e);
            throw new RuntimeException("DynamoDB table setup failed", e);
        } catch (Exception e) {
            log.error("Unexpected error during table setup: " + e.getMessage(), e);
            throw new RuntimeException("Unexpected error during DynamoDB table setup", e);
        }
    }

    public User create(User user) {
        log.infof("Creating user with chatId: %d, username: %s", user.getChatId(), user.getTelegramUsername());
        try {
            PutItemEnhancedRequest<User> request = PutItemEnhancedRequest.builder(User.class)
                    .item(user)
                    .build();
            userDynamoDbTable.putItem(request);
            log.infof("Successfully created user with chatId: %d", user.getChatId());
            return user;
        } catch (DynamoDbException e) {
            log.errorf("Failed to create user with chatId: %d - %s", user.getChatId(), e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public User findOne(final long chatId) {
        log.debugf("Finding user with chatId: %d", chatId);
        try {
            Key key = Key.builder()
                    .partitionValue(chatId)
                    .build();

            GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                    .key(key)
                    .build();

            User user = userDynamoDbTable.getItem(request);
            if (user != null) {
                log.debugf("Found user with chatId: %d", chatId);
            } else {
                log.infof("User not found with chatId: %d", chatId);
            }
            return user;
        } catch (DynamoDbException e) {
            log.errorf("Error finding user with chatId: %d - %s", chatId, e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public User update(final User user) {
        log.infof("Updating user with chatId: %d", user.getChatId());
        try {

            UpdateItemEnhancedRequest<User> request = UpdateItemEnhancedRequest.builder(User.class)
                    .item(user)
                    .ignoreNullsMode(IgnoreNullsMode.DEFAULT)
                    .build();

            User updatedUser = userDynamoDbTable.updateItem(request);
            log.infof("Successfully updated user with chatId: %d", user.getChatId());
            return updatedUser;
        } catch (DynamoDbException e) {
            log.errorf("Failed to update user with chatId: %d - %s", user.getChatId(), e.getMessage());
            throw new IllegalArgumentException("Error updating user: " + e.getMessage());
        }
    }
}
