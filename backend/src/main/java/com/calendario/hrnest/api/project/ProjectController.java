package com.calendario.hrnest.api.project;

import com.calendario.hrnest.application.project.CreateProjectCommand;
import com.calendario.hrnest.application.project.CreateProjectUseCase;
import com.calendario.hrnest.application.project.GetProjectTimeSummaryUseCase;
import com.calendario.hrnest.application.project.ListProjectsUseCase;
import com.calendario.hrnest.application.project.ProjectTimeSummaryView;
import com.calendario.hrnest.application.project.ProjectView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final ListProjectsUseCase listProjectsUseCase;
    private final GetProjectTimeSummaryUseCase getProjectTimeSummaryUseCase;

    public ProjectController(CreateProjectUseCase createProjectUseCase, ListProjectsUseCase listProjectsUseCase,
                              GetProjectTimeSummaryUseCase getProjectTimeSummaryUseCase) {
        this.createProjectUseCase = createProjectUseCase;
        this.listProjectsUseCase = listProjectsUseCase;
        this.getProjectTimeSummaryUseCase = getProjectTimeSummaryUseCase;
    }

    @PostMapping
    public ResponseEntity<ProjectView> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectView created = createProjectUseCase.execute(
                new CreateProjectCommand(request.name(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProjectView>> listAll() {
        return ResponseEntity.ok(listProjectsUseCase.execute());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ProjectTimeSummaryView> summary(@PathVariable Long id) {
        return ResponseEntity.ok(getProjectTimeSummaryUseCase.execute(id));
    }
}
