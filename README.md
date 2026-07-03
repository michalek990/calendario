# Calendario — HR Management System

Aplikacja typu HRnest: zarządzanie personelem, wnioskami urlopowymi i raportowaniem czasu pracy.

## Stack

- **Backend:** Spring Boot 3.3, Java 21, Maven, Spring Security (JWT), Spring Data JPA
- **Frontend:** React (Vite) + TypeScript
- **Baza danych:** PostgreSQL (H2 w testach)

## Struktura repo

```
Calendario/
├── backend/    # Spring Boot API
└── frontend/   # React SPA
```

## Moduły

| Moduł | Opis |
|---|---|
| Users | Użytkownicy, role, działy |
| Leave | Wnioski urlopowe, salda urlopowe |
| Time Tracking | Rejestracja czasu pracy, raporty |

Szczegółowa dokumentacja backendu: [backend/README.md](backend/README.md)
