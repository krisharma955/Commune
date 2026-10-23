package com.k955.Commune.dtos.Workspace;

import jakarta.validation.constraints.NotBlank;

public record WorkspaceRequest(
        @NotBlank String name,
        String description
) {
}
