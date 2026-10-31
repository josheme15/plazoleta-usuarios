package com.plazoleta.usuarios.infrastructure.security;

import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.domain.spi.ITokenGeneratorPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService implements ITokenGeneratorPort {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USER_ID = "id";
    public static final String CLAIM_RESTAURANT_ID = "restaurantId";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name());
        if (user.getRestaurantId() != null) {
            // Para empleados: plazoleta sabra de que restaurante son sin consultar a usuarios.
            builder.claim(CLAIM_RESTAURANT_ID, user.getRestaurantId());
        }
        return builder
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Valida firma y expiracion. Lanza {@link io.jsonwebtoken.JwtException} si el token no es valido.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
