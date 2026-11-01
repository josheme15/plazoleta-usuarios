package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.api.IUserServicePort;
import com.plazoleta.usuarios.domain.exception.DocumentAlreadyExistsException;
import com.plazoleta.usuarios.domain.exception.EmailAlreadyExistsException;
import com.plazoleta.usuarios.domain.exception.InvalidDocumentException;
import com.plazoleta.usuarios.domain.exception.InvalidEmailException;
import com.plazoleta.usuarios.domain.exception.InvalidPhoneException;
import com.plazoleta.usuarios.domain.exception.InvalidRoleException;
import com.plazoleta.usuarios.domain.exception.NotRestaurantOwnerException;
import com.plazoleta.usuarios.domain.exception.RequiredFieldException;
import com.plazoleta.usuarios.domain.exception.RestaurantNotFoundException;
import com.plazoleta.usuarios.domain.exception.UnderageUserException;
import com.plazoleta.usuarios.domain.exception.UserNotFoundException;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IAuthenticatedUserPort;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.IRestaurantClientPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import com.plazoleta.usuarios.domain.validation.EmailValidator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class UserUseCase implements IUserServicePort {

    // Maximo 13 caracteres: '+' opcional seguido de hasta 12 digitos, o hasta 13 digitos sin '+'.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+\\d{1,12}|\\d{1,13})$");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("^\\d+$");
    private static final int ADULT_AGE = 18;

    private final IUserPersistencePort userPersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;
    private final IRestaurantClientPort restaurantClientPort;
    private final IAuthenticatedUserPort authenticatedUserPort;
    private final Clock clock;

    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IPasswordEncoderPort passwordEncoderPort,
                       IRestaurantClientPort restaurantClientPort,
                       IAuthenticatedUserPort authenticatedUserPort,
                       Clock clock) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.restaurantClientPort = restaurantClientPort;
        this.authenticatedUserPort = authenticatedUserPort;
        this.clock = clock;
    }

    @Override
    public User createOwner(User user) {
        validateRequiredFields(user, true);
        validateFormats(user);
        validateAdult(user.getBirthDate());
        validateUniqueEmailAndDocument(user);

        user.setRole(Role.OWNER);
        return saveWithEncodedPassword(user);
    }

    @Override
    public User createEmployee(User employee, Long roleId) {
        validateRequiredFields(employee, false);
        if (roleId == null) {
            throw new RequiredFieldException("idRol");
        }
        if (employee.getRestaurantId() == null) {
            throw new RequiredFieldException("idRestaurante");
        }
        validateFormats(employee);
        if (!Role.EMPLOYEE.getId().equals(roleId)) {
            throw new InvalidRoleException("empleado", Role.EMPLOYEE.getId());
        }
        validateRestaurantOwner(employee.getRestaurantId());
        validateUniqueEmailAndDocument(employee);

        employee.setRole(Role.EMPLOYEE);
        return saveWithEncodedPassword(employee);
    }

    @Override
    public User createClient(User client, Long roleId) {
        validateRequiredFields(client, false);
        if (roleId == null) {
            throw new RequiredFieldException("idRol");
        }
        validateFormats(client);
        if (!Role.CLIENT.getId().equals(roleId)) {
            throw new InvalidRoleException("cliente", Role.CLIENT.getId());
        }
        validateUniqueEmailAndDocument(client);

        client.setRole(Role.CLIENT);
        // Un cliente no pertenece a ningun restaurante.
        client.setRestaurantId(null);
        return saveWithEncodedPassword(client);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new RequiredFieldException("id");
        }
        return userPersistencePort.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private void validateRequiredFields(User user, boolean birthDateRequired) {
        requireText(user.getFirstName(), "nombre");
        requireText(user.getLastName(), "apellido");
        requireText(user.getDocumentNumber(), "documentoDeIdentidad");
        requireText(user.getPhone(), "celular");
        if (birthDateRequired && user.getBirthDate() == null) {
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

    private void validateFormats(User user) {
        if (!EmailValidator.isValid(user.getEmail())) {
            throw new InvalidEmailException();
        }
        if (!PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            throw new InvalidPhoneException();
        }
        if (!DOCUMENT_PATTERN.matcher(user.getDocumentNumber()).matches()) {
            throw new InvalidDocumentException();
        }
    }

    private void validateAdult(LocalDate birthDate) {
        if (Period.between(birthDate, LocalDate.now(clock)).getYears() < ADULT_AGE) {
            throw new UnderageUserException();
        }
    }

    private void validateRestaurantOwner(Long restaurantId) {
        Long ownerId = restaurantClientPort.findRestaurantOwnerId(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);
        if (!ownerId.equals(authenticatedUserPort.getAuthenticatedUserId())) {
            throw new NotRestaurantOwnerException();
        }
    }

    private void validateUniqueEmailAndDocument(User user) {
        if (userPersistencePort.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        if (userPersistencePort.existsByDocumentNumber(user.getDocumentNumber())) {
            throw new DocumentAlreadyExistsException();
        }
    }

    private User saveWithEncodedPassword(User user) {
        user.setPassword(passwordEncoderPort.encode(user.getPassword()));
        return userPersistencePort.saveUser(user);
    }
}
