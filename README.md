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
| Users / Auth (register, login) | ✅ | ✅ (logowanie/rejestracja, profil użytkownika) |
| Leave (wnioski urlopowe) | ✅ | ✅ (złóż wniosek, lista własnych, zatwierdź/odrzuć dla MANAGER/HR_ADMIN, widoczne na kalendarzu pulpitu) |
| Time Tracking (czas pracy) | ✅ | ✅ (clock-in/clock-out, lista własnych wpisów, widoczne na kalendarzu pulpitu) |
| Ustawienia konta | — | ⏳ (placeholder w nawigacji, czeka na endpointy backendu) |

Nawigacja frontendu w kolorystyce granat/biel/żółty (patrz
[frontend/README.md](frontend/README.md#wygląd)).

Szczegółowa dokumentacja: [backend/README.md](backend/README.md) ·
[frontend/README.md](frontend/README.md)

## Uruchomienie przez Docker Compose

Cały stack (PostgreSQL + backend + frontend) jedną komendą:

```bash
docker compose up -d --build
```

| Serwis | Adres | Uwagi |
|---|---|---|
| frontend (nginx) | http://localhost:3000 | serwuje SPA, proxuje `/api/**` do backendu |
| backend | http://localhost:8080 | Spring Boot API |
| postgres | localhost:5432 | baza `hrnest`, user/hasło `hrnest`/`hrnest` (tylko dev) |

Backend startuje z `spring.jpa.hibernate.ddl-auto=update` (nadpisane przez
zmienną środowiskową w `docker-compose.yml`), więc schemat tabel tworzy się
automatycznie przy pierwszym starcie — bez ręcznych migracji. `JWT_SECRET`
można nadpisać zmienną środowiskową hosta przed odpaleniem; domyślny sekret
jest tylko na potrzeby dev/testów.

Zatrzymanie: `docker compose down` (dodaj `-v`, żeby usunąć też wolumen z
danymi Postgresa).
