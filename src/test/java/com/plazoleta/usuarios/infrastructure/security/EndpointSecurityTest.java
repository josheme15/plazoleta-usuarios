package com.plazoleta.usuarios.infrastructure.security;

import com.plazoleta.usuarios.application.dto.response.LoginResponseDto;
import com.plazoleta.usuarios.application.dto.response.UserResponseDto;
import com.plazoleta.usuarios.application.handler.IAuthHandler;
import com.plazoleta.usuarios.application.handler.IUserHandler;
import com.plazoleta.usuarios.domain.model.Role;
import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.infrastructure.input.rest.AuthRestController;
import com.plazoleta.usuarios.infrastructure.input.rest.UserRestController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-5: verifica que cada endpoint exija token valido y el rol correspondiente.
 * Usa el filtro JWT y las reglas reales de SecurityConfig; los handlers son mocks.
 */
@WebMvcTest(controllers = {UserRestController.class, AuthRestController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "security.jwt.secret=cGxhem9sZXRhLWRlLWNvbWlkYXMtc2VjcmV0by1zdXBlci1zZWd1cm8tMjU2LWJpdHM=",
        "security.jwt.expiration-ms=60000"
})
class EndpointSecurityTest {

    private static final String OWNER_BODY = """
            {"firstName":"Juan","lastName":"Perez","documentNumber":"1234567890","phone":"+573005698325",
             "birthDate":"1990-05-20","email":"juan@correo.com","password":"Clave123*"}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private IUserHandler userHandler;

    @MockitoBean
    private IAuthHandler authHandler;

    private String bearer(Role role) {
        User user = new User();
        user.setId(1L);
        user.setEmail(role.name().toLowerCase() + "@plazoleta.com");
        user.setRole(role);
        return "Bearer " + jwtService.generateToken(user);
    }

    // ---------- POST /auth/login: publico ----------

    @Test
    void login_isPublic_noTokenRequired() throws Exception {
        when(authHandler.login(any())).thenReturn(new LoginResponseDto("jwt", 1L, "ADMIN"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@plazoleta.com\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_withInvalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"Admin123*\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /users/owner: solo ADMIN ----------

    @Test
    void createOwner_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/users/owner").contentType(MediaType.APPLICATION_JSON).content(OWNER_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOwner_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(post("/users/owner").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.firma")
                        .contentType(MediaType.APPLICATION_JSON).content(OWNER_BODY))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OWNER", "EMPLOYEE", "CLIENT"})
    void createOwner_withNonAdminRole_returns403(Role role) throws Exception {
        mockMvc.perform(post("/users/owner").header(HttpHeaders.AUTHORIZATION, bearer(role))
                        .contentType(MediaType.APPLICATION_JSON).content(OWNER_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOwner_withAdminRole_returns201() throws Exception {
        when(userHandler.createOwner(any())).thenReturn(new UserResponseDto());

        mockMvc.perform(post("/users/owner").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(OWNER_BODY))
                .andExpect(status().isCreated());
    }

    // ---------- GET /users/{id}: cualquier usuario autenticado ----------

    @Test
    void getUserById_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/users/1")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void getUserById_withAnyAuthenticatedRole_returns200(Role role) throws Exception {
        when(userHandler.getUserById(anyLong())).thenReturn(new UserResponseDto());

        mockMvc.perform(get("/users/1").header(HttpHeaders.AUTHORIZATION, bearer(role)))
                .andExpect(status().isOk());
    }
}
