package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para que un cliente cree su propia cuenta")
public class ClientRequestDto extends UserBaseRequestDto {

    @Schema(example = "4", description = "Id del rol: debe ser 4 (cliente)")
    @NotNull(message = "El idRol es obligatorio")
    private Long roleId;
}
