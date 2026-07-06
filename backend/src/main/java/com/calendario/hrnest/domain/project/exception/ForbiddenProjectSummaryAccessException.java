package com.calendario.hrnest.domain.project.exception;

public class ForbiddenProjectSummaryAccessException extends RuntimeException {

    public ForbiddenProjectSummaryAccessException() {
        super("Tylko przełożony (MANAGER), dział kadr (HR) lub administrator (ADMIN) "
                + "może przeglądać zbiorcze podsumowanie czasu pracy nad projektem");
    }
}
