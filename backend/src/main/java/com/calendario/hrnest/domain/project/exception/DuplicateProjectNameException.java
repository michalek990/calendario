package com.calendario.hrnest.domain.project.exception;

public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String name) {
        super("Projekt o nazwie \"" + name + "\" już istnieje");
    }
}
