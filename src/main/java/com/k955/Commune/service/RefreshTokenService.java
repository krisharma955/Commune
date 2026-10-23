package com.k955.Commune.service;

import com.k955.Commune.dtos.UserSession.UserSessionResponse;
import com.k955.Commune.entity.User;
import com.k955.Commune.entity.UserSession;

import java.time.Instant;

public interface RefreshTokenService {

    String generateRefreshToken(); // Generate a secure random token

    String hashRefreshToken(String refreshToken); // Hash refresh Token using SHA-256

    Instant getRefreshTokenExpiry(); // Return current time + expiration

    String createSession(User user); // Generate refresh Token (save hash & expiry) & return the raw token

    UserSessionResponse validateRefreshToken(String refreshToken, UserSession userSession); // Check hash match, expiry and revocation

    String rotateRefreshToken(String refreshToken, UserSession userSession); // Replace old hash with new hash + return new

    void revokeSession(UserSession userSession); // Mark session as revoked

    void revokeAllSessions(User user); // Revoke every active session for this user

}
