package com.plazoleta.usuarios.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum Role {
    ADMIN(1L),
    OWNER(2L),
    EMPLOYEE(3L),
    CLIENT(4L);

    private final Long id;

    Role(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static Optional<Role> fromId(Long id) {
        return Arrays.stream(values()).filter(role -> role.id.equals(id)).findFirst();
    }
}
