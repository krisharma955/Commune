package com.k955.Commune.controller;

import com.k955.Commune.dtos.Workspace.WorkspaceRequest;
import com.k955.Commune.dtos.Workspace.WorkspaceResponse;
import com.k955.Commune.dtos.Workspace.WorkspaceUpdateRequest;
import com.k955.Commune.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody WorkspaceRequest workspaceRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.createWorkspace(workspaceRequest));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(workspaceService.getWorkspaceById(workspaceId));
    }

    @GetMapping
    public ResponseEntity<Page<WorkspaceResponse>> getUserWorkspaces(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 5, direction = Sort.Direction.DESC, sort = "updatedAt") Pageable pageable
    ) {
        return ResponseEntity.ok(workspaceService.getUserWorkspaces(slug, search, pageable));
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspaceById(
            @PathVariable UUID workspaceId, @RequestBody WorkspaceUpdateRequest workspaceUpdateRequest
    ) {
        return ResponseEntity.ok(workspaceService.updateWorkspaceById(workspaceId, workspaceUpdateRequest));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspaceById(@PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspaceById(workspaceId);
        return ResponseEntity.noContent().build();
    }

}
