package com.plazoleta.usuarios.domain.api;

import com.plazoleta.usuarios.domain.model.AuthenticatedSession;

public interface IAuthServicePort {

    /**
     * Valida correo y contrasena. Los intentos son ilimitados: un intento fallido no bloquea la cuenta.
     */
    AuthenticatedSession login(String email, String password);
}
