package com.calendario.hrnest.api.timetracking;

import java.time.Instant;

/** Ciało żądania opcjonalne — {@code clockOut} pozwala podać własną godzinę zakończenia zamiast bieżącej chwili serwera. */
public record ClockOutRequest(Instant clockOut) {
}
