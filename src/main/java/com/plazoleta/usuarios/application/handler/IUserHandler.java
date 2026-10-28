package com.plazoleta.usuarios.application.handler;

import com.plazoleta.usuarios.application.dto.request.OwnerRequestDto;
import com.plazoleta.usuarios.application.dto.response.UserResponseDto;

public interface IUserHandler {

    UserResponseDto createOwner(OwnerRequestDto ownerRequestDto);
}
