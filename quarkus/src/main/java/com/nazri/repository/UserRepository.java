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
        try {
            PutItemEnhancedRequest<User> request = PutItemEnhancedRequest.builder(User.class)
                    .item(user)
                    .build();
            userDynamoDbTable.putItem(request);
            return user;
        } catch (DynamoDbException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public User findOne(final long chatId) {
        try {
            Key key = Key.builder()
                    .partitionValue(chatId)
                    .build();

            GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                    .key(key)
                    .build();

            return userDynamoDbTable.getItem(request);
        } catch (DynamoDbException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public User update(final User user) {
        try {

            UpdateItemEnhancedRequest<User> request = UpdateItemEnhancedRequest.builder(User.class)
                    .item(user)
                    .ignoreNullsMode(IgnoreNullsMode.DEFAULT)
                    .build();

            return userDynamoDbTable.updateItem(request);
        } catch (DynamoDbException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Error updating user: " + e.getMessage());
        }
    }
}
