package com.plazoleta.usuarios.domain.exception;

public class NotRestaurantOwnerException extends DomainException {

    public NotRestaurantOwnerException() {
        super("Solo el propietario del restaurante puede crear empleados para ese restaurante");
    }
}
