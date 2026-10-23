package com.k955.Commune.mapper;

import com.k955.Commune.dtos.UserSession.UserSessionResponse;
import com.k955.Commune.entity.UserSession;

public interface UserSessionMapper {

    UserSessionResponse toUserSessionResponse(UserSession userSession);

}
