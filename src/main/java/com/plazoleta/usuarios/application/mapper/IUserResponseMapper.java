package com.plazoleta.usuarios.application.mapper;

import com.plazoleta.usuarios.application.dto.response.UserResponseDto;
import com.plazoleta.usuarios.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserResponseMapper {

    UserResponseDto toResponse(User user);
}
