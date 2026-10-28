package com.plazoleta.usuarios.infrastructure.configuration;

import com.plazoleta.usuarios.domain.api.IAuthServicePort;
import com.plazoleta.usuarios.domain.api.IUserServicePort;
import com.plazoleta.usuarios.domain.spi.IPasswordEncoderPort;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import com.plazoleta.usuarios.domain.spi.IUserPersistencePort;
import com.plazoleta.usuarios.domain.usecase.AuthUseCase;
import com.plazoleta.usuarios.domain.usecase.UserUseCase;
import com.plazoleta.usuarios.infrastructure.output.jpa.adapter.UserJpaAdapter;
import com.plazoleta.usuarios.infrastructure.output.jpa.mapper.IUserEntityMapper;
import com.plazoleta.usuarios.infrastructure.output.jpa.repository.IUserRepository;
import com.plazoleta.usuarios.infrastructure.security.BCryptPasswordEncoderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final IUserRepository userRepository;
    private final IUserEntityMapper userEntityMapper;
    private final PasswordEncoder passwordEncoder;
    private final ITokenGeneratorPort tokenGeneratorPort;

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public IUserPersistencePort userPersistencePort() {
        return new UserJpaAdapter(userRepository, userEntityMapper);
    }

    @Bean
    public IPasswordEncoderPort passwordEncoderPort() {
        return new BCryptPasswordEncoderAdapter(passwordEncoder);
    }

    @Bean
    public IUserServicePort userServicePort() {
        return new UserUseCase(userPersistencePort(), passwordEncoderPort(), clock());
    }

    @Bean
    public IAuthServicePort authServicePort() {
        return new AuthUseCase(userPersistencePort(), passwordEncoderPort(), tokenGeneratorPort);
    }
}
