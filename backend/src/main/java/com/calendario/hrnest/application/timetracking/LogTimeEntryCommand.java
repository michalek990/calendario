package com.calendario.hrnest.application.timetracking;

import java.time.Instant;

/**
 * Ręczna rejestracja czasu pracy — dokładne chwile rozpoczęcia/zakończenia.
 * Przeliczenie "dzień + godzina" na {@link Instant} robi frontend (zna strefę
 * czasową przeglądarki użytkownika) — backend celowo nie zgaduje strefy czasowej
 * serwera, żeby uniknąć przesunięcia godzin względem tego, co wpisał użytkownik.
 */
public record LogTimeEntryCommand(
        Instant clockIn,
        Instant clockOut,
        Integer breakMinutes,
        Long projectId
) {
}
