package com.plazoleta.usuarios.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("No existe un usuario con el id indicado");
    }
}
