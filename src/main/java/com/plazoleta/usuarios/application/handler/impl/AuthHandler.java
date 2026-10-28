package com.plazoleta.usuarios.application.handler.impl;

import com.plazoleta.usuarios.application.dto.request.LoginRequestDto;
import com.plazoleta.usuarios.application.dto.response.LoginResponseDto;
import com.plazoleta.usuarios.application.handler.IAuthHandler;
import com.plazoleta.usuarios.domain.api.IAuthServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthHandler implements IAuthHandler {

    private final IAuthServicePort authServicePort;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        return new LoginResponseDto(
                authServicePort.login(loginRequestDto.getEmail(), loginRequestDto.getPassword()));
    }
}
