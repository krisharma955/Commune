package com.k955.Commune.service;

import com.k955.Commune.dtos.Auth.*;
import jakarta.validation.Valid;

public interface AuthService {

    AuthResponse signup(@Valid SignupRequest signupRequest);

    AuthResponse login(@Valid LoginRequest loginRequest);

    AuthResponse refresh(RefreshTokenRequest refreshTokenRequest);

    void logout(RefreshTokenRequest refreshTokenRequest);

    void logoutAll();

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest);

}
