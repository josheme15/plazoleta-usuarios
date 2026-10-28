package com.plazoleta.usuarios.application.handler;

import com.plazoleta.usuarios.application.dto.request.LoginRequestDto;
import com.plazoleta.usuarios.application.dto.response.LoginResponseDto;

public interface IAuthHandler {

    LoginResponseDto login(LoginRequestDto loginRequestDto);
}
