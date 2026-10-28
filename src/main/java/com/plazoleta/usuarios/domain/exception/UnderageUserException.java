package com.plazoleta.usuarios.domain.exception;

public class UnderageUserException extends DomainException {

    public UnderageUserException() {
        super("El usuario debe ser mayor de edad");
    }
}
