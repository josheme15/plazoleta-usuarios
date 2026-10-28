package com.plazoleta.usuarios.domain.spi;

import com.plazoleta.usuarios.domain.model.User;

public interface ITokenGeneratorPort {

    String generateToken(User user);
}
