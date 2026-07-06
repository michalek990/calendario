package com.calendario.hrnest.api.timetracking;

/** Ciało żądania opcjonalne — brak body (lub {@code projectId: null}) oznacza wpis bez przypisanego projektu. */
public record ClockInRequest(Long projectId) {
}
