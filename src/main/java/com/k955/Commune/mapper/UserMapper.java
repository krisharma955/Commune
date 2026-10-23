package com.k955.Commune.mapper;

import com.k955.Commune.dtos.User.UserProfileResponse;
import com.k955.Commune.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);

}
