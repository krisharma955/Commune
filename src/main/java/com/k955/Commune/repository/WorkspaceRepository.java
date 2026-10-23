package com.k955.Commune.repository;

import com.k955.Commune.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID>, JpaSpecificationExecutor<Workspace> {

    @Query(
            value = """
            SELECT w
            FROM Workspace w
            WHERE EXISTS (
                SELECT 1
                FROM WorkspaceMember wm
                WHERE wm.workspace = w
                  AND wm.user.id = :userId
            )
            AND (CAST(:slug AS string) IS NULL OR w.slug = :slug)
            AND (
                CAST(:search AS string) IS NULL
                OR LOWER(w.name) LIKE CAST(:search AS string)
                OR LOWER(COALESCE(w.description, '')) LIKE CAST(:search AS string)
            )
            """,
            countQuery = """
            SELECT COUNT(w)
            FROM Workspace w
            WHERE EXISTS (
                SELECT 1
                FROM WorkspaceMember wm
                WHERE wm.workspace = w
                  AND wm.user.id = :userId
            )
            AND (CAST(:slug AS string) IS NULL OR w.slug = :slug)
            AND (
                CAST(:search AS string) IS NULL
                OR LOWER(w.name) LIKE CAST(:search AS string)
                OR LOWER(COALESCE(w.description, '')) LIKE CAST(:search AS string)
            )
            """
    )
    Page<Workspace> findUserWorkspaces(
            @Param("userId") UUID userId,
            @Param("slug") String slug,
            @Param("search") String searchPattern, // already lowercased, e.g. "%foo%"
            Pageable pageable
    );

}
