package com.plazoleta.usuarios.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear un propietario")
public class OwnerRequestDto extends UserBaseRequestDto {

    @Schema(example = "1990-05-20", type = "string", format = "date")
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
