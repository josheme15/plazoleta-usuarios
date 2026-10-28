package com.plazoleta.usuarios.domain.usecase;

import com.plazoleta.usuarios.domain.api.IAuthServicePort;
import com.plazoleta.usuarios.domain.exception.InvalidCredentialsException;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;

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
    public String login(String email, String password) {
        User user = userPersistencePort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoderPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return tokenGeneratorPort.generateToken(user);
    }
}
