package com.nazri.service;

import com.nazri.model.User;
import com.nazri.repository.UserRepository;
import com.nazri.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.userRepository = userRepository;
    }

    @Test
    void create_ShouldCreateUserWithDefaultValues_WhenChatProvided() {
        // Given
        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn(12345L);
        when(chat.getUserName()).thenReturn("test_user");
        
        User expectedUser = createTestUser();
        when(userRepository.create(any(User.class))).thenReturn(expectedUser);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(Util::getCurrentTime).thenReturn("2023-01-01T00:00:00Z");

            // When
            User result = userService.create(chat);

            // Then
            assertNotNull(result);
            assertEquals(12345L, result.getChatId());
            assertEquals("test_user", result.getTelegramUsername());
            assertEquals(Arrays.asList("SGD"), result.getOutputCurrency());
            assertEquals("2023-01-01T00:00:00Z", result.getCreatedDate());
            assertEquals("2023-01-01T00:00:00Z", result.getUpdatedDate());
            
            verify(userRepository).create(any(User.class));
        }
    }

    @Test
    void create_ShouldHandleNullUsername_WhenChatHasNoUsername() {
        // Given
        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn(12345L);
        when(chat.getUserName()).thenReturn(null);
        
        User expectedUser = createTestUser();
        expectedUser.setTelegramUsername(null);
        when(userRepository.create(any(User.class))).thenReturn(expectedUser);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(Util::getCurrentTime).thenReturn("2023-01-01T00:00:00Z");

            // When
            User result = userService.create(chat);

            // Then
            assertNotNull(result);
            assertEquals(12345L, result.getChatId());
            assertNull(result.getTelegramUsername());
            assertEquals(Arrays.asList("SGD"), result.getOutputCurrency());
            
            verify(userRepository).create(any(User.class));
        }
    }

    @Test
    void findOne_ShouldReturnUser_WhenUserExists() {
        // Given
        long chatId = 12345L;
        User expectedUser = createTestUser();
        when(userRepository.findOne(chatId)).thenReturn(expectedUser);

        // When
        User result = userService.findOne(chatId);

        // Then
        assertNotNull(result);
        assertEquals(expectedUser.getChatId(), result.getChatId());
        assertEquals(expectedUser.getTelegramUsername(), result.getTelegramUsername());
        verify(userRepository).findOne(chatId);
    }

    @Test
    void findOne_ShouldReturnNull_WhenUserDoesNotExist() {
        // Given
        long chatId = 12345L;
        when(userRepository.findOne(chatId)).thenReturn(null);

        // When
        User result = userService.findOne(chatId);

        // Then
        assertNull(result);
        verify(userRepository).findOne(chatId);
    }

    @Test
    void update_ShouldRemoveDuplicatesAndUpdateUser_WhenUserHasDuplicateCurrencies() {
        // Given
        User user = createTestUser();
        user.setOutputCurrency(Arrays.asList("USD", "EUR", "USD", "SGD", "EUR"));
        
        User updatedUser = createTestUser();
        updatedUser.setOutputCurrency(Arrays.asList("USD", "EUR", "SGD"));
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.update(user);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getOutputCurrency().size());
        assertTrue(result.getOutputCurrency().contains("USD"));
        assertTrue(result.getOutputCurrency().contains("EUR"));
        assertTrue(result.getOutputCurrency().contains("SGD"));
        
        verify(userRepository).update(any(User.class));
    }

    @Test
    void update_ShouldUpdateUser_WhenUserHasNoDuplicateCurrencies() {
        // Given
        User user = createTestUser();
        user.setOutputCurrency(Arrays.asList("USD", "EUR", "SGD"));
        
        User updatedUser = createTestUser();
        updatedUser.setOutputCurrency(Arrays.asList("USD", "EUR", "SGD"));
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.update(user);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getOutputCurrency().size());
        assertEquals(Arrays.asList("USD", "EUR", "SGD"), result.getOutputCurrency());
        
        verify(userRepository).update(any(User.class));
    }

    @Test
    void update_ShouldHandleEmptyOutputCurrency_WhenUserHasEmptyList() {
        // Given
        User user = createTestUser();
        user.setOutputCurrency(Arrays.asList());
        
        User updatedUser = createTestUser();
        updatedUser.setOutputCurrency(Arrays.asList());
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.update(user);

        // Then
        assertNotNull(result);
        assertTrue(result.getOutputCurrency().isEmpty());
        
        verify(userRepository).update(any(User.class));
    }

    @Test
    void update_ShouldHandleNullOutputCurrency_WhenUserHasNullList() {
        // Given
        User user = createTestUser();
        user.setOutputCurrency(null);
        
        // When & Then
        assertThrows(NullPointerException.class, () -> userService.update(user));
        
        verify(userRepository, never()).update(any(User.class));
    }

    private User createTestUser() {
        User user = new User();
        user.setChatId(12345L);
        user.setTelegramUsername("test_user");
        user.setInputCurrency("USD");
        user.setOutputCurrency(Arrays.asList("SGD"));
        user.setCreatedDate("2023-01-01T00:00:00Z");
        user.setUpdatedDate("2023-01-01T00:00:00Z");
        user.setBetaTester(false);
        return user;
    }
}
