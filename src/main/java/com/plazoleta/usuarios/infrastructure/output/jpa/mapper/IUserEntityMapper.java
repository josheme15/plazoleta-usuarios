package com.plazoleta.usuarios.infrastructure.output.jpa.mapper;

import com.plazoleta.usuarios.domain.model.User;
import com.plazoleta.usuarios.infrastructure.output.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserEntityMapper {

    UserEntity toEntity(User user);

    User toUser(UserEntity userEntity);
}
