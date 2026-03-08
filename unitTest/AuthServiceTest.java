package com.smartsplit.auth;

import com.smartsplit.security.JwtService;
import com.smartsplit.user.User;
import com.smartsplit.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// These are the DTOs your AuthService uses.
// public record RegisterRequest(String name, String email, String password) {}
// public record AuthResponse(String token) {}

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Create mocks for all dependencies of AuthService.
    // These are fake objects that we can control.
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    // This creates an instance of AuthService and injects the mocks defined above.
    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Create a common request object for all tests.
        registerRequest = new RegisterRequest("Meena", "meena@example.com", "password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // --- GIVEN ---
        // Define the behavior of our mocks for the "happy path" scenario.
        String dummyEncodedPassword = "encodedPassword";
        String dummyJwtToken = "jwt-token-string";

        // 1. When findByEmail is called, pretend the user doesn't exist.
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        // 2. When the password encoder is called, return our dummy encoded password.
        when(passwordEncoder.encode(registerRequest.password())).thenReturn(dummyEncodedPassword);
        // 3. When the JWT service is asked to generate a token, return our dummy token.
        when(jwtService.generateToken(any(User.class))).thenReturn(dummyJwtToken);

        // --- WHEN ---
        // Call the method we are testing.
        AuthResponse response = authService.register(registerRequest);

        // --- THEN ---
        // Assert that the results are what we expect.
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(dummyJwtToken);

        // Use an ArgumentCaptor to "capture" the User object that was passed to the save method.
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        // Assert that the saved user has the correct details.
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo(registerRequest.name());
        assertThat(savedUser.getEmail()).isEqualTo(registerRequest.email());
        assertThat(savedUser.getPassword()).isEqualTo(dummyEncodedPassword);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsTaken() {
        // --- GIVEN ---
        // For this scenario, pretend the user's email already exists in the database.
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(new User()));

        // --- WHEN & THEN ---
        // Assert that calling the register method throws the expected exception.
        assertThrows(IllegalStateException.class, () -> authService.register(registerRequest));

        // Verify that the save method and token generation were never called.
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }
}