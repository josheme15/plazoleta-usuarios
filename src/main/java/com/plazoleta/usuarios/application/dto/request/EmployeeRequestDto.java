package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear un empleado de un restaurante del propietario autenticado")
public class EmployeeRequestDto {

    @Schema(example = "Maria")
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @Schema(example = "Lopez")
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Schema(example = "1098765432", description = "Solo numerico")
    @NotBlank(message = "El documento de identidad es obligatorio")
    @Pattern(regexp = "^\\d+$", message = "El documento de identidad debe ser numerico")
    private String documentNumber;

    @Schema(example = "+573001234567", description = "Maximo 13 caracteres, puede iniciar con +")
    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^(\\+\\d{1,12}|\\d{1,13})$",
            message = "El celular debe tener maximo 13 caracteres, solo digitos y opcionalmente el simbolo + al inicio")
    private String phone;

    @Schema(example = "maria.lopez@correo.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electronico no tiene un formato valido")
    private String email;

    @Schema(example = "3", description = "Id del rol: debe ser 3 (empleado)")
    @NotNull(message = "El idRol es obligatorio")
    private Long roleId;

    @Schema(example = "Clave123*")
    @NotBlank(message = "La clave es obligatoria")
    private String password;

    @Schema(example = "1", description = "Id de un restaurante del propietario autenticado")
    @NotNull(message = "El id del restaurante es obligatorio")
    @Positive(message = "El id del restaurante debe ser positivo")
    private Long restaurantId;
}
