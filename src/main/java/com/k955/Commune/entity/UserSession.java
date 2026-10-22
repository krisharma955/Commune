package com.k955.Commune.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, unique = true)
    String refreshToken;

    @Column(nullable = false)
    Instant expiresAt;

    Instant revokedAt;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    Instant lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if(createdAt == null) createdAt = Instant.now();
    }

}
