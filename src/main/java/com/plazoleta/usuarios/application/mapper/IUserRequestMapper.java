package com.plazoleta.usuarios.application.mapper;

import com.plazoleta.usuarios.application.dto.request.OwnerRequestDto;
import com.plazoleta.usuarios.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(OwnerRequestDto ownerRequestDto);
}
