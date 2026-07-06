package com.calendario.hrnest.application.project;

import com.calendario.hrnest.domain.project.Project;
import java.time.Instant;

public record ProjectView(
        Long id,
        String name,
        String description,
        Instant createdAt
) {

    public static ProjectView from(Project project) {
        return new ProjectView(project.getId(), project.getName(), project.getDescription(), project.getCreatedAt());
    }
}
