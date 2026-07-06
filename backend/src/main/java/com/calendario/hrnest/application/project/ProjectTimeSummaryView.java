package com.calendario.hrnest.application.project;

/** Zbiorczy czas pracy zarejestrowany na projekcie (tylko zamknięte wpisy — otwarte nie mają jeszcze totalMinutes). */
public record ProjectTimeSummaryView(
        Long projectId,
        String projectName,
        long totalMinutes,
        long entryCount
) {
}
