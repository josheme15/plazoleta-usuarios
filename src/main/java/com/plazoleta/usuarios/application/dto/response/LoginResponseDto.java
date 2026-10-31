package com.plazoleta.usuarios.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sesion iniciada: token JWT y datos para mostrar las opciones del rol")
public class LoginResponseDto {

    @Schema(description = "JWT a enviar en el header Authorization: Bearer <token>")
    private String token;

    @Schema(example = "2")
    private Long userId;

    @Schema(example = "OWNER", description = "ADMIN, OWNER, EMPLOYEE o CLIENT")
    private String role;

    @Schema(example = "1", description = "Solo para empleados: restaurante al que pertenecen (null en otros roles)")
    private Long restaurantId;
}
