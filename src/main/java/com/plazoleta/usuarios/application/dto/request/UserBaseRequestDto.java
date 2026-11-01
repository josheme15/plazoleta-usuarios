package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Campos y validaciones comunes a la creacion de propietarios, empleados y clientes.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class UserBaseRequestDto {

    @Schema(example = "Juan")
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @Schema(example = "Perez")
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Schema(example = "1234567890", description = "Solo numerico")
    @NotBlank(message = "El documento de identidad es obligatorio")
    @Pattern(regexp = "^\\d+$", message = "El documento de identidad debe ser numerico")
    private String documentNumber;

    @Schema(example = "+573005698325", description = "Maximo 13 caracteres, puede iniciar con +")
    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^(\\+\\d{1,12}|\\d{1,13})$",
            message = "El celular debe tener maximo 13 caracteres, solo digitos y opcionalmente el simbolo + al inicio")
    private String phone;

    @Schema(example = "juan.perez@correo.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electronico no tiene un formato valido")
    private String email;

    @Schema(example = "Clave123*")
    @NotBlank(message = "La clave es obligatoria")
    private String password;
}
