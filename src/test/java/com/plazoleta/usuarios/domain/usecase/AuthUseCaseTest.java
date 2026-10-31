package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.InvalidCredentialsException;
import com.plazoleta.usuarios.domain.exception.InvalidEmailException;
import com.plazoleta.usuarios.domain.exception.RequiredFieldException;
import com.plazoleta.usuarios.domain.model.AuthenticatedSession;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    private User storedUser(Role role) {
        return new User(1L, "Admin", "Plazoleta", "1000000000", "+570000000000",
                LocalDate.of(1990, 1, 1), EMAIL, ENCODED_PASSWORD, role);
    }

    @Test
    void login_withValidCredentials_returnsSessionWithTokenIdAndRole() {
        User user = storedUser(Role.ADMIN);
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenGeneratorPort.generateToken(user)).thenReturn("jwt-token");

        AuthenticatedSession session = authUseCase.login(EMAIL, RAW_PASSWORD);

        assertEquals("jwt-token", session.getToken());
        assertEquals(1L, session.getUserId());
        assertEquals(Role.ADMIN, session.getRole());
        assertEquals(null, session.getRestaurantId());
    }

    @Test
    void login_asEmployee_returnsSessionWithRestaurantId() {
        User employee = storedUser(Role.EMPLOYEE);
        employee.setRestaurantId(10L);
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenGeneratorPort.generateToken(employee)).thenReturn("jwt-empleado");

        AuthenticatedSession session = authUseCase.login(EMAIL, RAW_PASSWORD);

        assertEquals(Role.EMPLOYEE, session.getRole());
        assertEquals(10L, session.getRestaurantId());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void login_worksForEveryRole_andReturnsThatRole(Role role) {
        User user = storedUser(role);
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenGeneratorPort.generateToken(user)).thenReturn("jwt-" + role);

        assertEquals(role, authUseCase.login(EMAIL, RAW_PASSWORD).getRole());
    }

    @Test
    void login_whenUserDoesNotExist_throwsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, RAW_PASSWORD));
        verify(tokenGeneratorPort, never()).generateToken(any());
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(storedUser(Role.ADMIN)));
        when(passwordEncoderPort.matches("otra", ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, "otra"));
        verify(tokenGeneratorPort, never()).generateToken(any());
    }

    @Test
    void login_attemptsAreUnlimited_failedAttemptsDoNotBlockTheAccount() {
        User user = storedUser(Role.OWNER);
        when(userPersistencePort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(anyString(), any())).thenReturn(false);

        for (int attempt = 0; attempt < 10; attempt++) {
            assertThrows(InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, "incorrecta"));
        }

        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenGeneratorPort.generateToken(user)).thenReturn("jwt-token");

        assertEquals("jwt-token", authUseCase.login(EMAIL, RAW_PASSWORD).getToken());
        verify(userPersistencePort, times(11)).findByEmail(EMAIL);
        verify(userPersistencePort, never()).saveUser(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void login_withBlankEmail_throwsRequiredField(String email) {
        assertThrows(RequiredFieldException.class, () -> authUseCase.login(email, RAW_PASSWORD));
        verify(userPersistencePort, never()).findByEmail(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void login_withBlankPassword_throwsRequiredField(String password) {
        assertThrows(RequiredFieldException.class, () -> authUseCase.login(EMAIL, password));
        verify(userPersistencePort, never()).findByEmail(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "admin@", "@plazoleta.com", "admin plazoleta@correo.com", "admin@plazoleta"})
    void login_withInvalidEmailFormat_throwsInvalidEmail(String email) {
        assertThrows(InvalidEmailException.class, () -> authUseCase.login(email, RAW_PASSWORD));
        verify(userPersistencePort, never()).findByEmail(any());
    }
}
