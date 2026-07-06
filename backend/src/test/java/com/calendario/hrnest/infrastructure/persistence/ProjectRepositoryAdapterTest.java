package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.project.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ProjectRepositoryAdapter.class)
class ProjectRepositoryAdapterTest {

    @Autowired
    private ProjectRepositoryAdapter adapter;

    @Test
    void save_thenFindById_roundTripsAllFields() {
        Project project = Project.create("Kalendario", "Aplikacja HR");

        Project saved = adapter.save(project);

        assertThat(saved.getId()).isNotNull();

        Project found = adapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Kalendario");
        assertThat(found.getDescription()).isEqualTo("Aplikacja HR");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findAll_returnsAllSavedProjects() {
        adapter.save(Project.create("Alfa", null));
        adapter.save(Project.create("Beta", null));

        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    void existsByName_reflectsSavedProjects() {
        adapter.save(Project.create("Kalendario", null));

        assertThat(adapter.existsByName("Kalendario")).isTrue();
        assertThat(adapter.existsByName("Nieistniejacy")).isFalse();
    }
}
