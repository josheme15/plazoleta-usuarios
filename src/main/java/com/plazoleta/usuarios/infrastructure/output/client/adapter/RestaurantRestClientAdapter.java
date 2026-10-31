package com.plazoleta.usuarios.infrastructure.output.client.adapter;

import com.plazoleta.usuarios.domain.spi.IRestaurantClientPort;
import com.plazoleta.usuarios.infrastructure.output.client.dto.RestaurantClientResponseDto;
import com.plazoleta.usuarios.infrastructure.output.client.exception.PlazoletaServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Consulta el microservicio de plazoleta reenviando el JWT de la peticion actual.
 */
@RequiredArgsConstructor
public class RestaurantRestClientAdapter implements IRestaurantClientPort {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient plazoletaRestClient;

    @Override
    public Optional<Long> findRestaurantOwnerId(Long restaurantId) {
        try {
            RestaurantClientResponseDto restaurant = plazoletaRestClient.get()
                    .uri("/restaurants/{id}", restaurantId)
                    .headers(headers -> currentToken()
                            .ifPresent(token -> headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)))
                    .retrieve()
                    .body(RestaurantClientResponseDto.class);
            return Optional.ofNullable(restaurant).map(RestaurantClientResponseDto::getOwnerId);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new PlazoletaServiceUnavailableException(e);
        }
    }

    private Optional<String> currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            return Optional.of(token);
        }
        return Optional.empty();
    }
}
