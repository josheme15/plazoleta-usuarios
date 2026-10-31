package com.plazoleta.usuarios.infrastructure.output.client.adapter;

import com.plazoleta.usuarios.infrastructure.output.client.exception.PlazoletaServiceUnavailableException;
import com.plazoleta.usuarios.infrastructure.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestaurantRestClientAdapterTest {

    private static final String BASE_URL = "http://plazoleta";
    private static final String TOKEN = "jwt-del-propietario";

    private MockRestServiceServer server;
    private RestaurantRestClientAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new RestaurantRestClientAdapter(builder.build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(2L, "dueno@correo.com"), TOKEN, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findRestaurantOwnerId_whenRestaurantExists_returnsOwnerIdAndForwardsToken() {
        server.expect(requestTo(BASE_URL + "/restaurants/10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andRespond(withSuccess("{\"id\":10,\"name\":\"El Sabor\",\"ownerId\":2}", MediaType.APPLICATION_JSON));

        assertEquals(Optional.of(2L), adapter.findRestaurantOwnerId(10L));
        server.verify();
    }

    @Test
    void findRestaurantOwnerId_whenRestaurantDoesNotExist_returnsEmpty() {
        server.expect(requestTo(BASE_URL + "/restaurants/99")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertEquals(Optional.empty(), adapter.findRestaurantOwnerId(99L));
    }

    @Test
    void findRestaurantOwnerId_whenPlazoletaFails_throwsServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/restaurants/10")).andRespond(withServerError());

        assertThrows(PlazoletaServiceUnavailableException.class, () -> adapter.findRestaurantOwnerId(10L));
    }

    @Test
    void findRestaurantOwnerId_whenPlazoletaRejectsToken_throwsServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/restaurants/10")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(PlazoletaServiceUnavailableException.class, () -> adapter.findRestaurantOwnerId(10L));
    }
}
