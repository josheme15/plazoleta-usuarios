package com.plazoleta.usuarios.domain.exception;

public class InvalidRoleException extends DomainException {

    public InvalidRoleException(String expectedRoleName, Long expectedRoleId) {
        super("El idRol debe corresponder al rol " + expectedRoleName + " (" + expectedRoleId + ")");
    }
}
