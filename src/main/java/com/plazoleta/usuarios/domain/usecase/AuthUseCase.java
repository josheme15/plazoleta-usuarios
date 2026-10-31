package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.api.IAuthServicePort;
import com.plazoleta.usuarios.domain.exception.InvalidCredentialsException;
import com.plazoleta.usuarios.domain.exception.InvalidEmailException;
import com.plazoleta.usuarios.domain.exception.RequiredFieldException;
import com.plazoleta.usuarios.domain.model.AuthenticatedSession;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import com.plazoleta.usuarios.domain.validation.EmailValidator;

public class AuthUseCase implements IAuthServicePort {

    private final IUserPersistencePort userPersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;
    private final ITokenGeneratorPort tokenGeneratorPort;

    public AuthUseCase(IUserPersistencePort userPersistencePort,
                       IPasswordEncoderPort passwordEncoderPort,
                       ITokenGeneratorPort tokenGeneratorPort) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    public AuthenticatedSession login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new RequiredFieldException("correo");
        }
        if (password == null || password.isBlank()) {
            throw new RequiredFieldException("clave");
        }
        if (!EmailValidator.isValid(email)) {
            throw new InvalidEmailException();
        }

        // Mismo error si el correo no existe o la clave no coincide, para no revelar que correos estan registrados.
        User user = userPersistencePort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoderPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return new AuthenticatedSession(tokenGeneratorPort.generateToken(user), user.getId(), user.getRole(),
                user.getRestaurantId());
    }
}
