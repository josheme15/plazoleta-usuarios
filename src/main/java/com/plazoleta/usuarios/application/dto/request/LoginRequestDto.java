package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @Schema(example = "admin@plazoleta.com")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @Schema(example = "Admin123*")
    @NotBlank(message = "La clave es obligatoria")
    private String password;
}
