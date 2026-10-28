package com.plazoleta.usuarios.domain.spi;

import com.plazoleta.usuarios.domain.model.User;

import java.util.Optional;

public interface IUserPersistencePort {

    User saveUser(User user);

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
