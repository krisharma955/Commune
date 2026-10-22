package com.k955.Commune.entity;

import com.k955.Commune.enums.Workspace.Role;
import com.k955.Commune.enums.Workspace.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "workspace_invitations")
public class WorkspaceInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    Workspace workspace;

    @Column(nullable = false)
    String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    User invitedBy;

    @Column(nullable = false, unique = true)
    String inviteToken; //Unique hash of the secret invitation token

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Role role;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Status status = Status.PENDING;

    @Column(nullable = false)
    Instant expiresAt;

    Instant acceptedAt;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if(createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
