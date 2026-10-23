package com.k955.Commune.service.impl;

import com.k955.Commune.dtos.Workspace.WorkspaceRequest;
import com.k955.Commune.dtos.Workspace.WorkspaceResponse;
import com.k955.Commune.dtos.Workspace.WorkspaceUpdateRequest;
import com.k955.Commune.entity.User;
import com.k955.Commune.entity.Workspace;
import com.k955.Commune.entity.WorkspaceMember;
import com.k955.Commune.enums.Workspace.Role;
import com.k955.Commune.exception.BadRequestException;
import com.k955.Commune.exception.ErrorCode;
import com.k955.Commune.exception.ResourceNotFoundException;
import com.k955.Commune.mapper.WorkspaceMapper;
import com.k955.Commune.repository.UserRepository;
import com.k955.Commune.repository.WorkspaceMemberRepository;
import com.k955.Commune.repository.WorkspaceRepository;
import com.k955.Commune.security.JwtAuthUtil;
import com.k955.Commune.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final JwtAuthUtil jwtAuthUtil;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceRequest workspaceRequest) {
        UUID userId = jwtAuthUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND, "The User Details can't be loaded at the moment. Try Later!"
                ));

        Workspace workspace = Workspace.builder()
                .name(workspaceRequest.name())
                .description(workspaceRequest.description())
                .slug(generateSlug(workspaceRequest.name()))
                .createdBy(user)
                .build();
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(user)
                .role(Role.OWNER)
                .build();
        workspaceMemberRepository.save(workspaceMember);

        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID workspaceId) {
        validateWorkspaceMembership(workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WORKSPACE_NOT_FOUND, "The Workspace Details can't be loaded at the moment. Try Later!"
                ));

        return workspaceMapper.toWorkspaceResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkspaceResponse> getUserWorkspaces(String slug, String search, Pageable pageable) {
        UUID userId = jwtAuthUtil.getCurrentUserId();

        String pattern = (search == null || search.isBlank())
                ? null
                : "%" + search.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";

        return workspaceRepository
                .findUserWorkspaces(userId, slug, pattern, pageable)
                .map(workspaceMapper::toWorkspaceResponse);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspaceById(UUID workspaceId, WorkspaceUpdateRequest workspaceUpdateRequest) {
        validateWorkspaceAdmin(workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WORKSPACE_NOT_FOUND, "The Workspace Details can't be loaded at the moment. Try Later!"
                ));

        if(workspaceUpdateRequest.name() != null && !workspaceUpdateRequest.name().isBlank()) {
            workspace.setName(workspaceUpdateRequest.name());
        }

        if(workspaceUpdateRequest.description() != null && !workspaceUpdateRequest.description().isBlank()) {
            workspace.setDescription(workspaceUpdateRequest.description());
        }

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    @Override
    @Transactional
    public void deleteWorkspaceById(UUID workspaceId) {
        UUID userId = jwtAuthUtil.getCurrentUserId();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WORKSPACE_NOT_FOUND, "The Workspace Details can't be loaded at the moment. Try Later!"
                ));

        if(!workspace.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "The Workspace can only be deleted by the Owner");
        }

        workspaceMemberRepository.deleteAllByWorkspaceId(workspaceId);

        workspaceRepository.delete(workspace);
    }

    /// Utility Methods

    private String generateSlug(String name) {
        return name
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private void validateWorkspaceMembership(UUID workspaceId) {
        UUID userId = jwtAuthUtil.getCurrentUserId();

        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);

        if (!isMember) {
            throw new ResourceNotFoundException(ErrorCode.WORKSPACE_NOT_FOUND, "Workspace not found.");
        }
    }

    private void validateWorkspaceAdmin(UUID workspaceId) {
        UUID userId = jwtAuthUtil.getCurrentUserId();

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WORKSPACE_NOT_FOUND,
                        "Workspace not found."
                ));

        if (member.getRole() != Role.OWNER
                && member.getRole() != Role.ADMIN) {
            throw new BadRequestException(
                    ErrorCode.BAD_REQUEST,
                    "You don't have permission to update this workspace."
            );
        }
    }

}
