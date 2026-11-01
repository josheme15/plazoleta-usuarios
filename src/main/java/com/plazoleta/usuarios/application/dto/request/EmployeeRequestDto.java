package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear un empleado de un restaurante del propietario autenticado")
public class EmployeeRequestDto extends UserBaseRequestDto {

    @Schema(example = "3", description = "Id del rol: debe ser 3 (empleado)")
    @NotNull(message = "El idRol es obligatorio")
    private Long roleId;

    @Schema(example = "1", description = "Id de un restaurante del propietario autenticado")
    @NotNull(message = "El id del restaurante es obligatorio")
    @Positive(message = "El id del restaurante debe ser positivo")
    private Long restaurantId;
}
