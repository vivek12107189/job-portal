package com.vivek.jobportal.service;

import com.vivek.jobportal.config.JwtProperties;
import com.vivek.jobportal.dto.LoginRequest;
import com.vivek.jobportal.dto.RefreshTokenRequest;
import com.vivek.jobportal.dto.RegisterRequest;
import com.vivek.jobportal.entity.RefreshToken;
import com.vivek.jobportal.entity.Role;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.RefreshTokenRepository;
import com.vivek.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerDefaultsRoleToJobSeekerWhenRoleIsMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("user@test.com");
        request.setPassword("secret");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.JOB_SEEKER, userCaptor.getValue().getRole());
    }

    @Test
    void registerRejectsAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Admin User");
        request.setEmail("admin@test.com");
        request.setPassword("secret");
        request.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Existing User");
        request.setEmail("existing@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginStoresRefreshTokenAsHash() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        User user = User.builder()
                .email("user@test.com")
                .password("hashed-password")
                .role(Role.JOB_SEEKER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken("user@test.com", "JOB_SEEKER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("user@test.com", "JOB_SEEKER")).thenReturn("refresh-token");
        when(jwtProperties.expirationMs()).thenReturn(3_600_000L);
        when(jwtProperties.refreshExpirationMs()).thenReturn(1_209_600_000L);

        authService.login(request);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertNotEquals("refresh-token", tokenCaptor.getValue().getToken());
        assertEquals(64, tokenCaptor.getValue().getToken().length());
    }

    @Test
    void refreshLooksUpStoredHashedToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        User user = User.builder()
                .email("user@test.com")
                .role(Role.EMPLOYER)
                .build();

        RefreshToken storedToken = RefreshToken.builder()
                .token("stored-hash")
                .user(user)
                .revoked(false)
                .expiresAt(java.time.LocalDateTime.now().plusDays(1))
                .build();

        when(jwtService.validateToken("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken(eq("0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120")))
                .thenReturn(java.util.Optional.of(storedToken));
        when(jwtService.generateAccessToken("user@test.com", "EMPLOYER")).thenReturn("new-access");
        when(jwtService.generateRefreshToken("user@test.com", "EMPLOYER")).thenReturn("new-refresh");
        when(jwtProperties.expirationMs()).thenReturn(3_600_000L);
        when(jwtProperties.refreshExpirationMs()).thenReturn(1_209_600_000L);

        authService.refresh(request);

        verify(refreshTokenRepository).findByToken("0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120");
    }
}
