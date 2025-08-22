package com.nazri.repository;

import com.nazri.model.User;
import com.nazri.util.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private DynamoDbTable<User> userDynamoDbTable;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(dynamoDbEnhancedClient.table(eq(Constant.USER_TABLE), any(TableSchema.class)))
                .thenReturn(userDynamoDbTable);
        userRepository = new UserRepository(dynamoDbEnhancedClient);
    }

    @Test
    void constructor_ShouldInitializeSuccessfully() {
        // Given - mocks are already set up in setUp()
        
        // When - constructor is called in setUp()
        
        // Then
        assertNotNull(userRepository);
        verify(dynamoDbEnhancedClient).table(eq(Constant.USER_TABLE), any(TableSchema.class));
    }

    @Test
    void constructor_ShouldThrowRuntimeException_WhenDynamoDbExceptionOccurs() {
        // Given
        when(dynamoDbEnhancedClient.table(eq(Constant.USER_TABLE), any(TableSchema.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> new UserRepository(dynamoDbEnhancedClient));
        assertEquals("DynamoDB table setup failed", exception.getMessage());
    }

    @Test
    void constructor_ShouldThrowRuntimeException_WhenUnexpectedExceptionOccurs() {
        // Given
        when(dynamoDbEnhancedClient.table(eq(Constant.USER_TABLE), any(TableSchema.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> new UserRepository(dynamoDbEnhancedClient));
        assertEquals("Unexpected error during DynamoDB table setup", exception.getMessage());
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
        DynamoDbException dynamoDbException = (DynamoDbException) DynamoDbException.builder()
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
        DynamoDbException dynamoDbException = (DynamoDbException) DynamoDbException.builder()
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
        DynamoDbException dynamoDbException = (DynamoDbException) DynamoDbException.builder()
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
