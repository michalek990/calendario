package com.calendario.hrnest.domain.project.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long projectId) {
        super("Nie znaleziono projektu o id " + projectId);
    }
}
