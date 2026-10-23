package com.k955.Commune.service;

import com.k955.Commune.dtos.Workspace.WorkspaceRequest;
import com.k955.Commune.dtos.Workspace.WorkspaceResponse;
import com.k955.Commune.dtos.Workspace.WorkspaceUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(@Valid WorkspaceRequest workspaceRequest);

    WorkspaceResponse getWorkspaceById(UUID workspaceId);

    Page<WorkspaceResponse> getUserWorkspaces(String slug, String search, Pageable pageable);

    WorkspaceResponse updateWorkspaceById(UUID workspaceId, WorkspaceUpdateRequest workspaceUpdateRequest);

    void deleteWorkspaceById(UUID workspaceId);

}
