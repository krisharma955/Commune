package com.k955.Commune.dtos.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
