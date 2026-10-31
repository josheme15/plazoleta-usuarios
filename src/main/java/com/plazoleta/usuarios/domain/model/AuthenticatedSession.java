package com.plazoleta.usuarios.domain.model;

/**
 * Resultado de un inicio de sesion exitoso: el token y los datos que la app necesita para mostrar
 * las opciones del rol del usuario.
 */
public class AuthenticatedSession {

    private final String token;
    private final Long userId;
    private final Role role;
    // Solo para empleados: restaurante al que pertenecen (null para los demas roles).
    private final Long restaurantId;

    public AuthenticatedSession(String token, Long userId, Role role, Long restaurantId) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.restaurantId = restaurantId;
    }

    public String getToken() { return token; }

    public Long getUserId() { return userId; }

    public Role getRole() { return role; }

    public Long getRestaurantId() { return restaurantId; }
}
