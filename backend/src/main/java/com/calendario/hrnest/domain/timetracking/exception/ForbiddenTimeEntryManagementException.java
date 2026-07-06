package com.calendario.hrnest.domain.timetracking.exception;

public class ForbiddenTimeEntryManagementException extends RuntimeException {

    public ForbiddenTimeEntryManagementException() {
        super("Tylko przełożony (MANAGER), dział kadr (HR) lub administrator (ADMIN) może zarządzać czasem pracy innych pracowników");
    }
}
