# Calendario — HR Management System

Aplikacja typu HRnest: zarządzanie personelem, wnioskami urlopowymi, projektami i raportowaniem czasu pracy.

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
| Users / Auth (register, login, zmiana hasła) | ✅ | ✅ (logowanie/rejestracja, popup profilu w navbarze, zmiana hasła w ustawieniach) |
| Role EMPLOYEE / MANAGER / HR / ADMIN | ✅ | ✅ (`/admin/users` — lista wszystkich pracowników; `/admin/users/:id` — osobny widok edycji roli/danych organizacyjnych, patrz [frontend/README.md](frontend/README.md#role-employee--manager--hr--admin)) |
| Profil użytkownika — dane organizacyjne (stanowisko, dział, zakład, przełożony/podwładny) | ✅ | ✅ (`/profile`: podgląd własnych danych; `/admin/users/:id`: HR/ADMIN edytuje dane dowolnego pracownika z listy, bez ręcznego podawania id) |
| Profil użytkownika — dane personalne (data urodzenia, telefon, awatar, ostatnie logowanie) | ✅ | ✅ (`/profile`: podgląd i edycja własnych danych) |
| Leave (wnioski urlopowe: wypoczynkowy, na żądanie, chorobowy, bezpłatny, opieka nad dzieckiem bezpłatna, okolicznościowy, praca z domu, odbiór za święto, delegacja) | ✅ | ✅ (formularz nowego wniosku ze wszystkimi typami, lista własnych, zatwierdź/odrzuć dla MANAGER/HR/ADMIN, widoczne na kalendarzu pulpitu) |
| Zatwierdzanie wniosków — MANAGER tylko bezpośredni podwładni, HR tylko własny zakład, ADMIN każdy pracownik | ✅ | ✅ (`/team`: frontend woła te same endpointy, backend dodatkowo weryfikuje zakres) |
| Roczny limit urlopu wypoczynkowego (26 dni) i roczne podsumowanie (praca zdalna vs pozostałe) | ✅ | ✅ (widget podsumowania rocznego na `/leave-requests`) |
| Ostatnie zmiany na wnioskach (`/api/leave-requests/me/recent-activity`) | ✅ | ✅ (widget na pulpicie) |
| Powiadomienia w aplikacji i mailem o decyzji na wniosku | ✅ | ✅ (`/notifications` + licznik nieprzeczytanych w navbarze; e-mail jest tylko backendowy z natury) |
| Time Tracking (czas pracy: clock-in/out, log ręczny, korekta wpisu) | ✅ | ✅ (rozpocznij/zakończ pracę z wyborem projektu, lista własnych wpisów, widoczne na kalendarzu pulpitu) |
| Zarządzanie zespołem — wnioski i czas pracy podwładnych (`/team`) | ✅ | ✅ (MANAGER/HR/ADMIN: zatwierdzanie wniosków + podgląd i edycja wpisów czasu pracy zespołu; dla HR automatycznie zawężone do własnego zakładu przez backend) |
| Zakłady jako encja + zarządzanie nimi (`GET/POST/PUT/DELETE /api/facilities`) | ✅ | ✅ (`/admin/facilities`: zakładki wnioski/czas pracy wybranego zakładu + "Zarządzanie zakładami" — dodaj/zmień nazwę/usuń, ADMIN) |
| Zakres uprawnień HR wg własnego zakładu (lista pracowników, edycja profilu, wnioski, czas pracy) | ✅ | ✅ (bez zmian frontendu — backend sam zwraca HR-owi tylko dane jego zakładu) |
| Projekty + rejestrowanie czasu w projekcie + podsumowania | ✅ | ✅ (`/projects`: lista, tworzenie dla HR/ADMIN, własne i zespołowe podsumowanie czasu) |
| Ustawienia wyglądu (dark mode, czcionka, kolor wiodący) | — | ✅ (czysto frontendowe, `localStorage`) |

Nawigacja frontendu w domyślnej kolorystyce jasny niebieski/biel/żółty,
konfigurowalnej w `/settings` (dark mode, rozmiar czcionki, kolor wiodący) —
patrz [frontend/README.md](frontend/README.md#wygląd).

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

**Uwaga przy aktualizacji istniejącego wolumenu Postgresa.** `ddl-auto=update`
dodaje nowe kolumny/tabele, ale **nie aktualizuje** CHECK constraints
wygenerowanych przez Hibernate dla kolumn enumowych (`users.role`,
`leave_requests.type`). Jeśli wolumen `postgres-data` istniał już przed
rozszerzeniem `Role` (o `HR`/`ADMIN`) i `LeaveType` (o nowe typy wniosków),
baza nadal ma stary constraint i odrzuci próbę zapisania nowej wartości
(`23514` — `violates check constraint`). Jednorazowa naprawa istniejącego
wolumenu (bez utraty danych):
```sql
ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('EMPLOYEE','MANAGER','HR','ADMIN'));

ALTER TABLE leave_requests DROP CONSTRAINT leave_requests_type_check;
ALTER TABLE leave_requests ADD CONSTRAINT leave_requests_type_check
  CHECK (type IN ('VACATION','ON_DEMAND','SICK_LEAVE','UNPAID','CHILDCARE_UNPAID',
                   'OCCASIONAL','REMOTE_WORK','HOLIDAY_COMPENSATION','BUSINESS_TRIP','OTHER'));
```
Wolumeny utworzone od zera po tych zmianach (albo `docker compose down -v` +
`up`) nie mają tego problemu — Hibernate generuje constraint z aktualnego enuma.

Powiadomienia mailowe o decyzjach na wnioskach są domyślnie wyłączone
(`MAIL_ENABLED=false`) — aplikacja loguje treść zamiast wysyłać. Żeby włączyć
realną wysyłkę w produkcji, ustaw `MAIL_ENABLED=true` oraz `MAIL_HOST`,
`MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` jako zmienne
środowiskowe hosta przed `docker compose up`.

Zatrzymanie: `docker compose down` (dodaj `-v`, żeby usunąć też wolumen z
danymi Postgresa).
