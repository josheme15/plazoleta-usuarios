package com.plazoleta.usuarios.domain.model;

/**
 * Resultado de un inicio de sesion exitoso: el token y los datos que la app necesita para mostrar
 * las opciones del rol del usuario.
 */
public class AuthenticatedSession {

    private final String token;
    private final Long userId;
    private final Role role;

    public AuthenticatedSession(String token, Long userId, Role role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }

    public Long getUserId() { return userId; }

    public Role getRole() { return role; }
}
