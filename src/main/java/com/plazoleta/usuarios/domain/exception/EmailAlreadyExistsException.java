package com.plazoleta.usuarios.domain.exception;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException() {
        super("Ya existe un usuario registrado con ese correo");
    }
}
