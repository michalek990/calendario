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

| Moduł | Backend | Frontend |
|---|---|---|
| Users / Auth (register, login) | ✅ | ✅ (ekrany logowania/rejestracji) |
| Leave (wnioski urlopowe) | ✅ | ⏳ |
| Time Tracking (czas pracy) | ✅ | ⏳ |

Szczegółowa dokumentacja: [backend/README.md](backend/README.md) ·
[frontend/README.md](frontend/README.md)
