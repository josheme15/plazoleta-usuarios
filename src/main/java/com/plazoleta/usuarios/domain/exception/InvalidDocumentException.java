package com.plazoleta.usuarios.domain.exception;

public class InvalidDocumentException extends DomainException {

    public InvalidDocumentException() {
        super("El documento de identidad debe ser numerico");
    }
}
