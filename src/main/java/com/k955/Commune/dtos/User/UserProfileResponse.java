package com.k955.Commune.dtos.User;

import com.k955.Commune.enums.User.Status;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        Status status,
        Instant createdAt
) {
}
