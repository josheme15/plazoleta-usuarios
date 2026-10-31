package com.plazoleta.usuarios.infrastructure.security;

import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtServiceTest {

    private static final String SECRET = "cGxhem9sZXRhLWRlLWNvbWlkYXMtc2VjcmV0by1zdXBlci1zZWd1cm8tMjU2LWJpdHM=";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);

    private User user(Long id, Role role, Long restaurantId) {
        User user = new User();
        user.setId(id);
        user.setEmail(role.name().toLowerCase() + "@plazoleta.com");
        user.setRole(role);
        user.setRestaurantId(restaurantId);
        return user;
    }

    @Test
    void generateToken_containsSubjectIdAndRole() {
        Claims claims = jwtService.parseClaims(jwtService.generateToken(user(2L, Role.OWNER, null)));

        assertEquals("owner@plazoleta.com", claims.getSubject());
        assertEquals(2L, claims.get(JwtService.CLAIM_USER_ID, Long.class));
        assertEquals("OWNER", claims.get(JwtService.CLAIM_ROLE, String.class));
    }

    @Test
    void generateToken_forEmployee_includesRestaurantId() {
        Claims claims = jwtService.parseClaims(jwtService.generateToken(user(5L, Role.EMPLOYEE, 10L)));

        assertEquals("EMPLOYEE", claims.get(JwtService.CLAIM_ROLE, String.class));
        assertEquals(10L, claims.get(JwtService.CLAIM_RESTAURANT_ID, Long.class));
    }

    @Test
    void generateToken_forUserWithoutRestaurant_omitsRestaurantId() {
        Claims claims = jwtService.parseClaims(jwtService.generateToken(user(1L, Role.ADMIN, null)));

        assertFalse(claims.containsKey(JwtService.CLAIM_RESTAURANT_ID));
    }
}
