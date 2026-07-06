package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final SpringDataProjectRepository springDataProjectRepository;

    public ProjectRepositoryAdapter(SpringDataProjectRepository springDataProjectRepository) {
        this.springDataProjectRepository = springDataProjectRepository;
    }

    @Override
    public Project save(Project project) {
        return toDomain(springDataProjectRepository.save(toEntity(project)));
    }

    @Override
    public Optional<Project> findById(Long id) {
        return springDataProjectRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return springDataProjectRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataProjectRepository.existsByName(name);
    }

    private ProjectJpaEntity toEntity(Project project) {
        ProjectJpaEntity entity = new ProjectJpaEntity();
        entity.setId(project.getId());
        entity.setName(project.getName());
        entity.setDescription(project.getDescription());
        entity.setCreatedAt(project.getCreatedAt());
        return entity;
    }

    private Project toDomain(ProjectJpaEntity entity) {
        return Project.reconstitute(entity.getId(), entity.getName(), entity.getDescription(), entity.getCreatedAt());
    }
}
