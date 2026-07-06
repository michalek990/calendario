package com.calendario.hrnest.domain.leave;

public enum LeaveType {
    /** Urlop wypoczynkowy. */
    VACATION,
    /** Urlop na żądanie (art. 167(2) KP). */
    ON_DEMAND,
    /** Zwolnienie lekarskie. */
    SICK_LEAVE,
    /** Urlop bezpłatny. */
    UNPAID,
    /** Opieka nad dzieckiem — urlop bezpłatny. */
    CHILDCARE_UNPAID,
    /** Urlop okolicznościowy (ślub, narodziny dziecka, pogrzeb itp.). */
    OCCASIONAL,
    /** Praca zdalna / z domu (home office). */
    REMOTE_WORK,
    /** Odbiór dnia wolnego za pracę w święto. */
    HOLIDAY_COMPENSATION,
    /** Delegacja / podróż służbowa. */
    BUSINESS_TRIP,
    OTHER;

    /** Nazwa wniosku po polsku — do komunikatów w powiadomieniach/e-mailach. */
    public String polishLabel() {
        return switch (this) {
            case VACATION -> "urlop wypoczynkowy";
            case ON_DEMAND -> "urlop na żądanie";
            case SICK_LEAVE -> "zwolnienie lekarskie";
            case UNPAID -> "urlop bezpłatny";
            case CHILDCARE_UNPAID -> "opieka nad dzieckiem (bezpłatna)";
            case OCCASIONAL -> "urlop okolicznościowy";
            case REMOTE_WORK -> "praca z domu";
            case HOLIDAY_COMPENSATION -> "odbiór dnia wolnego za święto";
            case BUSINESS_TRIP -> "delegacja";
            case OTHER -> "wniosek";
        };
    }
}
