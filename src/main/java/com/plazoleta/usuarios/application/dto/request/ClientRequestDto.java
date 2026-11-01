package com.plazoleta.usuarios.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para que un cliente cree su propia cuenta")
public class ClientRequestDto {

    @Schema(example = "Carlos")
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @Schema(example = "Ramirez")
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Schema(example = "1122334455", description = "Solo numerico")
    @NotBlank(message = "El documento de identidad es obligatorio")
    @Pattern(regexp = "^\\d+$", message = "El documento de identidad debe ser numerico")
    private String documentNumber;

    @Schema(example = "+573109876543", description = "Maximo 13 caracteres, puede iniciar con +")
    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "^(\\+\\d{1,12}|\\d{1,13})$",
            message = "El celular debe tener maximo 13 caracteres, solo digitos y opcionalmente el simbolo + al inicio")
    private String phone;

    @Schema(example = "carlos.ramirez@correo.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electronico no tiene un formato valido")
    private String email;

    @Schema(example = "4", description = "Id del rol: debe ser 4 (cliente)")
    @NotNull(message = "El idRol es obligatorio")
    private Long roleId;

    @Schema(example = "Clave123*")
    @NotBlank(message = "La clave es obligatoria")
    private String password;
}
