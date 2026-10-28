package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.InvalidCredentialsException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    private static final String EMAIL = "admin@plazoleta.com";
    private static final String RAW_PASSWORD = "Admin123*";
    private static final String ENCODED_PASSWORD = "$2a$10$hash";

    @Mock
    private IUserPersistencePort userPersistencePort;

    @Mock
    private IPasswordEncoderPort passwordEncoderPort;

    @Mock
    private ITokenGeneratorPort tokenGeneratorPort;

    @InjectMocks
    private AuthUseCase authUseCase;

    private User storedUser() {
        return new User(1L, "Admin", "Plazoleta", "1000000000", "+570000000000",
                LocalDate.of(1990, 1, 1), EMAIL, ENCODED_PASSWORD, Role.ADMIN);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        User user = storedUser();
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenGeneratorPort.generateToken(user)).thenReturn("jwt-token");

        assertEquals("jwt-token", authUseCase.login(EMAIL, RAW_PASSWORD));
    }

    @Test
    void login_whenUserDoesNotExist_throwsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, RAW_PASSWORD));
        verify(tokenGeneratorPort, never()).generateToken(any());
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(storedUser()));
        when(passwordEncoderPort.matches("otra", ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, "otra"));
        verify(tokenGeneratorPort, never()).generateToken(any());
    }
}
