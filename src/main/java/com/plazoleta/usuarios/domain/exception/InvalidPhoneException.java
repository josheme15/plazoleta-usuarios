package com.plazoleta.usuarios.domain.exception;

public class InvalidPhoneException extends DomainException {

    public InvalidPhoneException() {
        super("El celular debe tener maximo 13 caracteres, solo digitos y opcionalmente el simbolo + al inicio");
    }
}
