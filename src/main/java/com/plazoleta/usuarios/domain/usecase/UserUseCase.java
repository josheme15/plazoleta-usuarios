package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.api.IUserServicePort;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class UserUseCase implements IUserServicePort {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // Maximo 13 caracteres: '+' opcional seguido de hasta 12 digitos, o hasta 13 digitos sin '+'.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+\\d{1,12}|\\d{1,13})$");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("^\\d+$");
    private static final int ADULT_AGE = 18;

    private final IUserPersistencePort userPersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;
    private final Clock clock;

    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IPasswordEncoderPort passwordEncoderPort,
                       Clock clock) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.clock = clock;
    }

    @Override
    public User createOwner(User user) {
        validateRequiredFields(user);
        validateEmail(user.getEmail());
        validatePhone(user.getPhone());
        validateDocument(user.getDocumentNumber());
        validateAdult(user.getBirthDate());

        if (userPersistencePort.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        if (userPersistencePort.existsByDocumentNumber(user.getDocumentNumber())) {
            throw new DocumentAlreadyExistsException();
        }

        user.setRole(Role.OWNER);
        user.setPassword(passwordEncoderPort.encode(user.getPassword()));
        return userPersistencePort.saveUser(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new RequiredFieldException("id");
        }
        return userPersistencePort.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private void validateRequiredFields(User user) {
        requireText(user.getFirstName(), "nombre");
        requireText(user.getLastName(), "apellido");
        requireText(user.getDocumentNumber(), "documentoDeIdentidad");
        requireText(user.getPhone(), "celular");
        if (user.getBirthDate() == null) {
            throw new RequiredFieldException("fechaNacimiento");
        }
        requireText(user.getEmail(), "correo");
        requireText(user.getPassword(), "clave");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldException(fieldName);
        }
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException();
        }
    }

    private void validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidPhoneException();
        }
    }

    private void validateDocument(String documentNumber) {
        if (!DOCUMENT_PATTERN.matcher(documentNumber).matches()) {
            throw new InvalidDocumentException();
        }
    }

    private void validateAdult(LocalDate birthDate) {
        if (Period.between(birthDate, LocalDate.now(clock)).getYears() < ADULT_AGE) {
            throw new UnderageUserException();
        }
    }
}
