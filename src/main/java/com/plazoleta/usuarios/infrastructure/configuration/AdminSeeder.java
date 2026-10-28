package com.plazoleta.usuarios.infrastructure.configuration;

import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Crea el usuario ADMINISTRADOR inicial, necesario para poder consumir el endpoint de crear propietario.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final IUserPersistencePort userPersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;

    @Value("${admin.seed.email}")
    private String adminEmail;

    @Value("${admin.seed.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userPersistencePort.existsByEmail(adminEmail)) {
            return;
        }
        userPersistencePort.saveUser(new User(null, "Admin", "Plazoleta", "1000000000", "+570000000000",
                LocalDate.of(1990, 1, 1), adminEmail, passwordEncoderPort.encode(adminPassword), Role.ADMIN));
    }
}
