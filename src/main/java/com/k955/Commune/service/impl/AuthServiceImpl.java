package com.k955.Commune.service.impl;

import com.k955.Commune.dtos.Auth.*;
import com.k955.Commune.entity.User;
import com.k955.Commune.entity.UserSession;
import com.k955.Commune.exception.BadRequestException;
import com.k955.Commune.exception.ErrorCode;
import com.k955.Commune.exception.InvalidRefreshTokenException;
import com.k955.Commune.exception.ResourceNotFoundException;
import com.k955.Commune.mapper.UserMapper;
import com.k955.Commune.repository.UserRepository;
import com.k955.Commune.repository.UserSessionRepository;
import com.k955.Commune.security.JwtAuthUtil;
import com.k955.Commune.service.AuthService;
import com.k955.Commune.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtAuthUtil jwtAuthUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionRepository userSessionRepository;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        boolean exists = userRepository.existsByEmail(signupRequest.email());
        if(exists) throw new BadRequestException(ErrorCode.USER_ALREADY_EXISTS, "An account with this email already exists.");

        User user = User.builder()
                .name(signupRequest.name())
                .email(signupRequest.email())
                .password(signupRequest.password())
                .build();
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        User savedUser = userRepository.save(user);

        String token = jwtAuthUtil.generateAccessToken(savedUser);
        String refreshToken = refreshTokenService.createSession(savedUser);

        return new AuthResponse(token, refreshToken, userMapper.toUserProfileResponse(savedUser));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_CREDENTIALS, "Invalid Email or Password.");
        }

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND, "User with email: " + loginRequest.email() + " not found."
                ));

        String token = jwtAuthUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createSession(user);

        return new AuthResponse(token, refreshToken, userMapper.toUserProfileResponse(user));
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest refreshTokenRequest) {
        if (refreshTokenRequest.refreshToken() == null || refreshTokenRequest.refreshToken().isBlank()) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.INVALID_REFRESH_TOKEN,
                    "Invalid refresh token."
            );
        }

        String refreshTokenHash = refreshTokenService.hashRefreshToken(refreshTokenRequest.refreshToken());

        UserSession userSession = userSessionRepository.findByRefreshToken(refreshTokenHash)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.NO_SESSION_FOUND, "No User Session Found!"
                ));

        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenRequest.refreshToken(), userSession);

        User user = userSession.getUser();

        String token = jwtAuthUtil.generateAccessToken(user);

        return new AuthResponse(token, newRefreshToken, userMapper.toUserProfileResponse(user));
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        if (refreshTokenRequest.refreshToken() == null || refreshTokenRequest.refreshToken().isBlank()) {
            return;
        }

        String refreshTokenHash = refreshTokenService.hashRefreshToken(refreshTokenRequest.refreshToken());

        userSessionRepository.findByRefreshToken(refreshTokenHash)
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        refreshTokenService.revokeSession(session);
                    }
                });
    }

    @Override
    @Transactional
    public void logoutAll() {
        User user = jwtAuthUtil.getCurrentUser();
        refreshTokenService.revokeAllSessions(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = jwtAuthUtil.getCurrentUser();

        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPassword())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect."
            );
        }

        if (passwordEncoder.matches(changePasswordRequest.newPassword(), user.getPassword())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_CREDENTIALS, "New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));

        userRepository.save(user);

        refreshTokenService.revokeAllSessions(user);
    }

}
