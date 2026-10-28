package com.plazoleta.usuarios.infrastructure.output.jpa.repository;

import com.plazoleta.usuarios.infrastructure.output.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<UserEntity> findByEmail(String email);
}
