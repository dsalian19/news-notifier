package com.newsnotifier.service;

import com.newsnotifier.dto.AuthResponse;
import com.newsnotifier.dto.RegisterRequest;
import com.newsnotifier.model.User;
import com.newsnotifier.repository.UserRepository;
import com.newsnotifier.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

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
        String testSecret = Base64.getEncoder().encodeToString(new byte[64]);
        JwtUtil jwtUtil = new JwtUtil(testSecret, 86400000);
        userService = new UserService(userRepository, new BCryptPasswordEncoder(), jwtUtil);
    }

    @Test
    void register_success() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(UUID.randomUUID())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .phoneNumber(u.getPhoneNumber())
                    .notifyEmail(u.isNotifyEmail())
                    .notifySms(u.isNotifySms())
                    .build();
            return u;
        });

        AuthResponse response = userService.register(new RegisterRequest("new@example.com", "secret123", "555-1234"));

        assertNotNull(response.token());
        assertEquals("new@example.com", response.email());
        assertFalse(response.notifyEmail());
        assertFalse(response.notifySms());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.register(new RegisterRequest("existing@example.com", "secret123", "555-1234")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }
}
