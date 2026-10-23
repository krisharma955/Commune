package com.k955.Commune.mapper;

import com.k955.Commune.dtos.UserSession.UserSessionResponse;
import com.k955.Commune.entity.UserSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSessionMapper {

    UserSessionResponse toUserSessionResponse(UserSession userSession);

}
