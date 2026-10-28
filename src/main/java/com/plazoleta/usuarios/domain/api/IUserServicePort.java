package com.plazoleta.usuarios.domain.api;

import com.plazoleta.usuarios.domain.model.User;

public interface IUserServicePort {

    User createOwner(User user);
}
