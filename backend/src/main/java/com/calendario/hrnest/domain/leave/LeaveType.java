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
    OTHER
}
