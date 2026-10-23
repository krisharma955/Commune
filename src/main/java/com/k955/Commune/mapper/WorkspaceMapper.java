package com.k955.Commune.mapper;

import com.k955.Commune.dtos.Workspace.WorkspaceResponse;
import com.k955.Commune.entity.Workspace;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {

    WorkspaceResponse toWorkspaceResponse(Workspace workspace);

}
