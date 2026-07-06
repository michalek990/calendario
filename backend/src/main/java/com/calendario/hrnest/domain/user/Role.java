package com.calendario.hrnest.domain.user;

public enum Role {
    /** Pracownik. */
    EMPLOYEE,
    /** Przełożony — zatwierdza wnioski swoich bezpośrednich podwładnych (relacja przez {@code supervisorId}). */
    MANAGER,
    /** Dział kadr — zatwierdza wnioski dowolnego pracownika, zarządza danymi organizacyjnymi. */
    HR,
    /** Administrator systemu — pełne uprawnienia, w tym te dostępne dla HR. */
    ADMIN
}
