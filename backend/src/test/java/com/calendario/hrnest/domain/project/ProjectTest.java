package com.calendario.hrnest.domain.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void create_hasNoIdUntilPersisted() {
        Project project = Project.create("Kalendario", "Aplikacja HR");

        assertThat(project.getId()).isNull();
        assertThat(project.getName()).isEqualTo("Kalendario");
        assertThat(project.getDescription()).isEqualTo("Aplikacja HR");
        assertThat(project.getCreatedAt()).isNotNull();
    }
}
