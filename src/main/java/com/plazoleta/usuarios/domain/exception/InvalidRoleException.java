package com.plazoleta.usuarios.domain.exception;

public class InvalidRoleException extends DomainException {

    public InvalidRoleException(Long expectedRoleId) {
        super("El idRol debe corresponder al rol empleado (" + expectedRoleId + ")");
    }
}
