package com.calendario.hrnest.domain.leave.exception;

public class LeaveRequestNotFoundException extends RuntimeException {

    public LeaveRequestNotFoundException(Long id) {
        super("Nie znaleziono wniosku urlopowego o id " + id);
    }
}
