package com.plazoleta.usuarios.infrastructure.output.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de GET /restaurants/{id} del microservicio de plazoleta. Solo se mapean los campos necesarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantClientResponseDto {

    private Long id;
    private Long ownerId;
}
