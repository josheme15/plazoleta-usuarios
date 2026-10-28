package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.exception.DocumentAlreadyExistsException;
import com.plazoleta.usuarios.domain.exception.EmailAlreadyExistsException;
import com.plazoleta.usuarios.domain.exception.InvalidDocumentException;
import com.plazoleta.usuarios.domain.exception.InvalidEmailException;
import com.plazoleta.usuarios.domain.exception.InvalidPhoneException;
import com.plazoleta.usuarios.domain.exception.RequiredFieldException;
import com.plazoleta.usuarios.domain.exception.UnderageUserException;
import com.plazoleta.usuarios.domain.exception.UserNotFoundException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);
    private static final String RAW_PASSWORD = "Clave123*";
    private static final String ENCODED_PASSWORD = "$2a$10$hash";

    @Mock
    private IUserPersistencePort userPersistencePort;

    @Mock
    private IPasswordEncoderPort passwordEncoderPort;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        userUseCase = new UserUseCase(userPersistencePort, passwordEncoderPort, fixedClock);
    }

    private User validUser() {
        return new User(null, "Juan", "Perez", "1234567890", "+573005698325",
                LocalDate.of(1990, 5, 20), "juan.perez@correo.com", RAW_PASSWORD, null);
    }

    @Test
    void createOwner_withValidData_savesUserWithOwnerRoleAndEncryptedPassword() {
        User user = validUser();
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userUseCase.createOwner(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userPersistencePort).saveUser(captor.capture());
        assertEquals(Role.OWNER, captor.getValue().getRole());
        assertEquals(ENCODED_PASSWORD, captor.getValue().getPassword());
        assertSame(captor.getValue(), result);
    }

    @Test
    void createOwner_ignoresRoleSentByClient_alwaysAssignsOwner() {
        User user = validUser();
        user.setRole(Role.ADMIN);
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Role.OWNER, userUseCase.createOwner(user).getRole());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3005698325", "+573005698325", "1234567890123"})
    void createOwner_withValidPhones_succeeds(String phone) {
        User user = validUser();
        user.setPhone(phone);
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(phone, userUseCase.createOwner(user).getPhone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5730056983251", "12345678901234", "300abc8325", "57+3005698325", "+"})
    void createOwner_withInvalidPhone_throwsException(String phone) {
        User user = validUser();
        user.setPhone(phone);

        assertThrows(InvalidPhoneException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"juan.perez", "juan@", "@correo.com", "juan perez@correo.com", "juan@correo"})
    void createOwner_withInvalidEmail_throwsException(String email) {
        User user = validUser();
        user.setEmail(email);

        assertThrows(InvalidEmailException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345abc", "123-456", "12.345", "-123"})
    void createOwner_withNonNumericDocument_throwsException(String document) {
        User user = validUser();
        user.setDocumentNumber(document);

        assertThrows(InvalidDocumentException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    void createOwner_whenUserIsUnderage_throwsException() {
        User user = validUser();
        user.setBirthDate(TODAY.minusYears(17));

        assertThrows(UnderageUserException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    void createOwner_whenUserTurns18Tomorrow_throwsException() {
        User user = validUser();
        user.setBirthDate(TODAY.minusYears(18).plusDays(1));

        assertThrows(UnderageUserException.class, () -> userUseCase.createOwner(user));
    }

    @Test
    void createOwner_whenUserTurns18Today_succeeds() {
        User user = validUser();
        user.setBirthDate(TODAY.minusYears(18));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Role.OWNER, userUseCase.createOwner(user).getRole());
    }

    @Test
    void createOwner_whenEmailAlreadyExists_throwsException() {
        User user = validUser();
        when(userPersistencePort.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @Test
    void createOwner_whenDocumentAlreadyExists_throwsException() {
        User user = validUser();
        when(userPersistencePort.existsByEmail(user.getEmail())).thenReturn(false);
        when(userPersistencePort.existsByDocumentNumber(user.getDocumentNumber())).thenReturn(true);

        assertThrows(DocumentAlreadyExistsException.class, () -> userUseCase.createOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createOwner_withBlankFirstName_throwsException(String value) {
        User user = validUser();
        user.setFirstName(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOwner_withBlankLastName_throwsException(String value) {
        User user = validUser();
        user.setLastName(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOwner_withBlankDocument_throwsException(String value) {
        User user = validUser();
        user.setDocumentNumber(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOwner_withBlankPhone_throwsException(String value) {
        User user = validUser();
        user.setPhone(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOwner_withBlankEmail_throwsException(String value) {
        User user = validUser();
        user.setEmail(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOwner_withBlankPassword_throwsException(String value) {
        User user = validUser();
        user.setPassword(value);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
        verify(passwordEncoderPort, never()).encode(any());
    }

    @Test
    void createOwner_withNullBirthDate_throwsException() {
        User user = validUser();
        user.setBirthDate(null);

        assertThrows(RequiredFieldException.class, () -> userUseCase.createOwner(user));
    }

    @Test
    void getUserById_whenUserExists_returnsUser() {
        User stored = validUser();
        stored.setId(7L);
        stored.setRole(Role.OWNER);
        when(userPersistencePort.findById(7L)).thenReturn(Optional.of(stored));

        User result = userUseCase.getUserById(7L);

        assertSame(stored, result);
        assertEquals(Role.OWNER, result.getRole());
    }

    @Test
    void getUserById_whenUserDoesNotExist_throwsException() {
        when(userPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userUseCase.getUserById(99L));
    }

    @Test
    void getUserById_withNullId_throwsException() {
        assertThrows(RequiredFieldException.class, () -> userUseCase.getUserById(null));
        verify(userPersistencePort, never()).findById(any());
    }
}
