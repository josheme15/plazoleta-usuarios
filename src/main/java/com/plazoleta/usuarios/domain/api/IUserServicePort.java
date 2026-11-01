package com.plazoleta.usuarios.domain.api;

import com.plazoleta.usuarios.domain.model.User;

public interface IUserServicePort {

    User createOwner(User user);

    /**
     * Crea un empleado para un restaurante del propietario autenticado.
     *
     * @param roleId idRol enviado en la solicitud; debe corresponder al rol empleado.
     */
    User createEmployee(User employee, Long roleId);

    /**
     * Registro publico de un cliente (no requiere estar autenticado).
     *
     * @param roleId idRol enviado en la solicitud; debe corresponder al rol cliente.
     */
    User createClient(User client, Long roleId);

    User getUserById(Long id);
}
