package com.plazoleta.usuarios.application.handler.impl;

import com.plazoleta.usuarios.application.dto.request.OwnerRequestDto;
import com.plazoleta.usuarios.application.dto.response.UserResponseDto;
import com.plazoleta.usuarios.application.handler.IUserHandler;
import com.plazoleta.usuarios.application.mapper.IUserRequestMapper;
import com.plazoleta.usuarios.application.mapper.IUserResponseMapper;
import com.plazoleta.usuarios.domain.api.IUserServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserHandler implements IUserHandler {

    private final IUserServicePort userServicePort;
    private final IUserRequestMapper userRequestMapper;
    private final IUserResponseMapper userResponseMapper;

    @Override
    public UserResponseDto createOwner(OwnerRequestDto ownerRequestDto) {
        return userResponseMapper.toResponse(
                userServicePort.createOwner(userRequestMapper.toUser(ownerRequestDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userResponseMapper.toResponse(userServicePort.getUserById(id));
    }
}
