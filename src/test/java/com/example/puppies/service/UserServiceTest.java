package com.example.puppies.service;

import com.example.puppies.entity.UserEntity;
import com.example.puppies.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindById_Success() {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setName("User One");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserEntity> foundUser = userService.findById(userId);

        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testFindById_NotFound() {
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserEntity> foundUser = userService.findById(userId);

        assertFalse(foundUser.isPresent());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testFindByEmail_Success() {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName("User One");

        when(userRepository.findByEmail(email)).thenReturn(user);

        UserEntity foundUser = userService.findByEmail(email);

        assertEquals(email, foundUser.getEmail());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testFindByEmail_NotFound() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        UserEntity foundUser = userService.findByEmail(email);

        assertNull(foundUser);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testCreateUser_Success() {
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        user.setName("User One");

        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserEntity createdUser = userService.createUser(user);

        assertEquals("user@example.com", createdUser.getEmail());

        verify(userRepository, times(1)).save(user);
    }


    @Test
    public void testAuthenticateUser_Success() {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName("User One");

        when(userRepository.findByEmail(email)).thenReturn(user);

        UserEntity authenticatedUser = userService.authenticateUser(email);

        assertEquals(email, authenticatedUser.getEmail());
        assertEquals("User One", authenticatedUser.getName());
    }

    @Test
    public void testAuthenticateUser_NotFound() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        UserEntity authenticatedUser = userService.authenticateUser(email);

        assertNull(authenticatedUser);
    }
}
