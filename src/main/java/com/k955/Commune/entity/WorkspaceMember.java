package com.k955.Commune.entity;

import com.k955.Commune.enums.Workspace.Role;
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
@Table(
        name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workspace_user", columnNames = {"workspace_id", "user_id"}
        )
)
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Role role;

    @Column(nullable = false, updatable = false)
    Instant joinedAt;

    @PrePersist
    protected void onJoin() {
        if(joinedAt == null) joinedAt = Instant.now();
    }

}
