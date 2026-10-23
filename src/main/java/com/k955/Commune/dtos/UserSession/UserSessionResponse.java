package com.k955.Commune.dtos.UserSession;

import com.k955.Commune.dtos.User.UserProfileResponse;

import java.time.Instant;

public record UserSessionResponse(
        String refreshToken,
        Instant expiresAt,
        Instant createdAt,
        UserProfileResponse user
) {
}
