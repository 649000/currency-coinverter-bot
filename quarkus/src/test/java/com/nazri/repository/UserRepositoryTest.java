package com.nazri.repository;

import com.nazri.model.User;
import com.nazri.util.Constant;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserRepositoryTest {

    @InjectMock
    DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @InjectMock
    DynamoDbTable<User> userDynamoDbTable;

    @Inject
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(dynamoDbEnhancedClient.table(eq(Constant.USER_TABLE), any(TableSchema.class)))
                .thenReturn(userDynamoDbTable);
    }

    @Test
    void userRepository_ShouldBeInjected() {
        // Given & When - userRepository is injected by Quarkus
        
        // Then
        assertNotNull(userRepository);
    }

    @Test
    void create_ShouldReturnUser_WhenSuccessful() {
        // Given
        User user = createTestUser();
        doNothing().when(userDynamoDbTable).putItem(any(PutItemEnhancedRequest.class));

        // When
        User result = userRepository.create(user);

        // Then
        assertNotNull(result);
        assertEquals(user.getChatId(), result.getChatId());
        assertEquals(user.getTelegramUsername(), result.getTelegramUsername());
        verify(userDynamoDbTable).putItem(any(PutItemEnhancedRequest.class));
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenDynamoDbExceptionOccurs() {
        // Given
        User user = createTestUser();
        DynamoDbException dynamoDbException = DynamoDbException.builder()
                .message("DynamoDB create error")
                .build();
        doThrow(dynamoDbException).when(userDynamoDbTable).putItem(any(PutItemEnhancedRequest.class));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userRepository.create(user));
        assertEquals("DynamoDB create error", exception.getMessage());
        verify(userDynamoDbTable).putItem(any(PutItemEnhancedRequest.class));
    }

    @Test
    void findOne_ShouldReturnUser_WhenUserExists() {
        // Given
        long chatId = 12345L;
        User expectedUser = createTestUser();
        when(userDynamoDbTable.getItem(any(GetItemEnhancedRequest.class)))
                .thenReturn(expectedUser);

        // When
        User result = userRepository.findOne(chatId);

        // Then
        assertNotNull(result);
        assertEquals(expectedUser.getChatId(), result.getChatId());
        assertEquals(expectedUser.getTelegramUsername(), result.getTelegramUsername());
        verify(userDynamoDbTable).getItem(any(GetItemEnhancedRequest.class));
    }

    @Test
    void findOne_ShouldReturnNull_WhenUserDoesNotExist() {
        // Given
        long chatId = 12345L;
        when(userDynamoDbTable.getItem(any(GetItemEnhancedRequest.class)))
                .thenReturn(null);

        // When
        User result = userRepository.findOne(chatId);

        // Then
        assertNull(result);
        verify(userDynamoDbTable).getItem(any(GetItemEnhancedRequest.class));
    }

    @Test
    void findOne_ShouldThrowIllegalArgumentException_WhenDynamoDbExceptionOccurs() {
        // Given
        long chatId = 12345L;
        DynamoDbException dynamoDbException = DynamoDbException.builder()
                .message("DynamoDB find error")
                .build();
        when(userDynamoDbTable.getItem(any(GetItemEnhancedRequest.class)))
                .thenThrow(dynamoDbException);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userRepository.findOne(chatId));
        assertEquals("DynamoDB find error", exception.getMessage());
        verify(userDynamoDbTable).getItem(any(GetItemEnhancedRequest.class));
    }

    @Test
    void update_ShouldReturnUpdatedUser_WhenSuccessful() {
        // Given
        User user = createTestUser();
        User updatedUser = createTestUser();
        updatedUser.setTelegramUsername("updated_username");
        
        when(userDynamoDbTable.updateItem(any(UpdateItemEnhancedRequest.class)))
                .thenReturn(updatedUser);

        // When
        User result = userRepository.update(user);

        // Then
        assertNotNull(result);
        assertEquals(updatedUser.getChatId(), result.getChatId());
        assertEquals(updatedUser.getTelegramUsername(), result.getTelegramUsername());
        verify(userDynamoDbTable).updateItem(any(UpdateItemEnhancedRequest.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_WhenDynamoDbExceptionOccurs() {
        // Given
        User user = createTestUser();
        DynamoDbException dynamoDbException = DynamoDbException.builder()
                .message("DynamoDB update error")
                .build();
        when(userDynamoDbTable.updateItem(any(UpdateItemEnhancedRequest.class)))
                .thenThrow(dynamoDbException);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userRepository.update(user));
        assertEquals("Error updating user: DynamoDB update error", exception.getMessage());
        verify(userDynamoDbTable).updateItem(any(UpdateItemEnhancedRequest.class));
    }

    private User createTestUser() {
        User user = new User();
        user.setChatId(12345L);
        user.setTelegramUsername("test_user");
        user.setInputCurrency("USD");
        user.setOutputCurrency(Arrays.asList("SGD", "EUR"));
        user.setCreatedDate("2023-01-01T00:00:00Z");
        user.setUpdatedDate("2023-01-01T00:00:00Z");
        user.setBetaTester(false);
        return user;
    }
}
