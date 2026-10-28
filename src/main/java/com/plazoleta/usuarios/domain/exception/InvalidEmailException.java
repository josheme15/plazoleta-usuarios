package com.plazoleta.usuarios.domain.exception;

public class InvalidEmailException extends DomainException {

    public InvalidEmailException() {
        super("El correo electronico no tiene un formato valido");
    }
}
