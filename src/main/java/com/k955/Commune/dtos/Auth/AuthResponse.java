package com.k955.Commune.dtos.Auth;

import com.k955.Commune.dtos.User.UserProfileResponse;

public record AuthResponse(
        String token,
        String refreshToken,
        UserProfileResponse user
) {
}
