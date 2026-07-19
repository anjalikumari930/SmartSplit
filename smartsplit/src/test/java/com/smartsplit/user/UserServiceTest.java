package com.smartsplit.user;

import com.smartsplit.security.JwtService;
import com.smartsplit.user.dto.LoginRequest;
import com.smartsplit.user.dto.SignupRequest;
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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Meena");
        signupRequest.setEmail("meena@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("meena@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        userService.register(signupRequest);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo(signupRequest.getName());
        assertThat(savedUser.getEmail()).isEqualTo(signupRequest.getEmail());
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> userService.register(signupRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token-string");

        String token = userService.login(loginRequest);

        assertThat(token).isEqualTo("jwt-token-string");
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
    }
}