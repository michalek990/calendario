package com.calendario.hrnest.api.timetracking;

import java.time.Instant;

/**
 * Ciało żądania opcjonalne — brak body (lub {@code projectId: null}) oznacza wpis bez przypisanego projektu.
 * {@code clockIn} pozwala podać własną godzinę rozpoczęcia zamiast bieżącej chwili serwera.
 */
public record ClockInRequest(Long projectId, Instant clockIn) {
}
