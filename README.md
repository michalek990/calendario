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
| Role EMPLOYEE / MANAGER / HR / ADMIN | ✅ | — (rola widoczna w JWT, ale bez UI do zarządzania rolami) |
| Profil użytkownika — dane organizacyjne (stanowisko, dział, zakład, przełożony/podwładny) | ✅ | — (na razie tylko API, patrz [backend/README.md](backend/README.md#moduł-profil-użytkownika)) |
| Profil użytkownika — dane personalne (data urodzenia, telefon, awatar, ostatnie logowanie) | ✅ | — (na razie tylko API) |
| Leave (wnioski urlopowe: wypoczynkowy, na żądanie, chorobowy, bezpłatny, opieka nad dzieckiem bezpłatna, okolicznościowy, praca z domu, odbiór za święto, delegacja) | ✅ | ✅ (wyśrodkowany formularz nowego wniosku, lista własnych, zatwierdź/odrzuć dla MANAGER/HR/ADMIN, widoczne na kalendarzu pulpitu — nowe typy wniosków wymagają dodania ich do frontendu) |
| Zatwierdzanie wniosków — MANAGER tylko bezpośredni podwładni, HR/ADMIN każdy pracownik | ✅ | ✅ (frontend woła te same endpointy, backend teraz dodatkowo weryfikuje zakres) |
| Roczny limit urlopu wypoczynkowego (26 dni) i roczne podsumowanie (praca zdalna vs pozostałe) | ✅ | — (na razie tylko API) |
| Ostatnie zmiany na wnioskach (`/api/leave-requests/me/recent-activity`) | ✅ | — (na razie tylko API) |
| Powiadomienia w aplikacji i mailem o decyzji na wniosku | ✅ | — (na razie tylko API, patrz [backend/README.md](backend/README.md#moduł-powiadomienia)) |
| Time Tracking (czas pracy) | ✅ | ✅ (wyśrodkowana karta rozpocznij/zakończ pracę, lista własnych wpisów, widoczne na kalendarzu pulpitu — bez UI do wyboru projektu) |
| Projekty + rejestrowanie czasu w projekcie + podsumowania | ✅ | — (na razie tylko API, patrz [backend/README.md](backend/README.md#moduł-projekty)) |
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

Powiadomienia mailowe o decyzjach na wnioskach są domyślnie wyłączone
(`MAIL_ENABLED=false`) — aplikacja loguje treść zamiast wysyłać. Żeby włączyć
realną wysyłkę w produkcji, ustaw `MAIL_ENABLED=true` oraz `MAIL_HOST`,
`MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` jako zmienne
środowiskowe hosta przed `docker compose up`.

Zatrzymanie: `docker compose down` (dodaj `-v`, żeby usunąć też wolumen z
danymi Postgresa).
