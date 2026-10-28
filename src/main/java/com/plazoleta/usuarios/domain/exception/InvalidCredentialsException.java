package com.plazoleta.usuarios.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Correo o contrasena incorrectos");
    }
}
