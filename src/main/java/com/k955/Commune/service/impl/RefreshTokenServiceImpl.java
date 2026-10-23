package com.k955.Commune.service.impl;

import com.k955.Commune.dtos.UserSession.UserSessionResponse;
import com.k955.Commune.entity.User;
import com.k955.Commune.entity.UserSession;
import com.k955.Commune.enums.User.Status;
import com.k955.Commune.exception.ErrorCode;
import com.k955.Commune.exception.InvalidRefreshTokenException;
import com.k955.Commune.mapper.UserSessionMapper;
import com.k955.Commune.repository.UserRepository;
import com.k955.Commune.repository.UserSessionRepository;
import com.k955.Commune.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.auth.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;
    private final UserSessionMapper userSessionMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Override
    public String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    refreshToken.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    @Override
    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(refreshTokenExpiration);
    }

    @Override
    @Transactional
    public String createSession(User user) {
        String newRefreshToken = generateRefreshToken();
        UserSession userSession = UserSession.builder()
                .user(user)
                .refreshToken(hashRefreshToken(newRefreshToken))
                .expiresAt(getRefreshTokenExpiry())
                .build();
        userSessionRepository.save(userSession);
        return userSession.getRefreshToken();
    }

    @Override
    public UserSessionResponse validateRefreshToken(String refreshToken, UserSession userSession) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token"
            );
        }

        if (userSession == null) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token"
            );
        }

        String tokenHash = hashRefreshToken(refreshToken);

        if (!MessageDigest.isEqual(
                tokenHash.getBytes(StandardCharsets.US_ASCII),
                userSession.getRefreshToken().getBytes(StandardCharsets.US_ASCII)
        )) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token"
            );
        }

        if (userSession.getExpiresAt() == null ||
                !userSession.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token expired"
            );
        }

        if (userSession.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.REFRESH_TOKEN_REVOKED, "Refresh token revoked"
            );
        }

        if (userSession.getUser() == null ||
                userSession.getUser().getStatus() != Status.ACTIVE) {
            throw new InvalidRefreshTokenException(
                    ErrorCode.USER_NOT_PERMITTED, "User is not permitted to authenticate"
            );
        }

        return userSessionMapper.toUserSessionResponse(userSession);
    }

    @Override
    @Transactional
    public String rotateRefreshToken(String refreshToken, UserSession userSession) {
        validateRefreshToken(refreshToken, userSession);

        String newRefreshToken = generateRefreshToken();
        String newRefreshTokenHash = hashRefreshToken(newRefreshToken);

        userSession.setRefreshToken(newRefreshTokenHash);
        userSession.setExpiresAt(getRefreshTokenExpiry());
        userSession.setLastUsedAt(Instant.now());

        userSessionRepository.save(userSession);

        return newRefreshToken;
    }

    @Override
    @Transactional
    public void revokeSession(UserSession userSession) {
        userSession.setRevokedAt(Instant.now());
        userSession.setLastUsedAt(Instant.now());
        userSessionRepository.save(userSession);
    }

    @Override
    @Transactional
    public void revokeAllSessions(User user) {
        List<UserSession> userSessions = userSessionRepository.findListOfUserSessionsByUser(user);
        for(UserSession userSession : userSessions) {
            revokeSession(userSession);
        }
    }

}
