package com.plazoleta.usuarios.domain.exception;

public class DocumentAlreadyExistsException extends DomainException {

    public DocumentAlreadyExistsException() {
        super("Ya existe un usuario registrado con ese documento de identidad");
    }
}
