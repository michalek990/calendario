# Backend — Calendario HR API

Spring Boot 3.3 / Java 21 / Maven. Architektura: **Clean Architecture / Ports & Adapters**.

## Wymagania lokalne

- Java 21
- Maven 3.9+
- PostgreSQL (dev/prod) — testy używają H2 in-memory, nie wymagają bazy

### Uwaga (Windows + AVG Antivirus)

Jeśli masz zainstalowane AVG i Maven zwraca błąd `PKIX path building failed`
przy pobieraniu zależności — to AVG robi TLS interception i podmienia
certyfikaty SSL, którym JDK domyślnie nie ufa. Naprawione lokalnie przez
zaimportowanie certyfikatu root AVG do kopii `cacerts` w
`%USERPROFILE%\.m2\cacerts` i wskazanie jej przez `MAVEN_OPTS`
(zmienna środowiskowa użytkownika, ustawiona na stałe).

## Uruchomienie testów

```bash
cd backend
mvn test
```

## Architektura

Cztery warstwy, zależności zawsze do środka (`api`/`infrastructure` → `application` → `domain`,
nigdy odwrotnie):

```
com.calendario.hrnest
├── domain/            # czyste POJO, ZERO adnotacji JPA/Springa — reguły biznesowe i porty
│   └── user/
│       ├── User.java              # agregat, tworzony wyłącznie przez User.register(...) / reconstitute(...)
│       ├── Role.java              # enum: EMPLOYEE, MANAGER, HR_ADMIN
│       ├── UserRepository.java    # PORT — interfejs, bez śladu Spring Data
│       └── exception/             # EmailAlreadyExistsException, InvalidCredentialsException
│
├── application/        # Use Case'y — orkiestracja, zależą TYLKO od domain
│   └── auth/
│       ├── RegisterUserUseCase.java / LoginUseCase.java
│       ├── PasswordHasher.java    # PORT (strategy)
│       ├── TokenProvider.java     # PORT (strategy)
│       └── RegisterCommand / LoginCommand / AuthResult (DTO wewnętrzne use case'ów)
│
├── infrastructure/      # Adaptery — implementacje portów, szczegóły technologiczne
│   ├── persistence/
│   │   ├── UserJpaEntity.java             # encja JPA (tabela `users`)
│   │   ├── SpringDataUserRepository.java  # Spring Data (package-private, szczegół implementacyjny)
│   │   └── UserRepositoryAdapter.java     # implements domain.user.UserRepository, mapuje JPA <-> domain
│   └── security/
│       ├── BCryptPasswordHasher.java      # implements PasswordHasher
│       ├── JwtTokenProvider.java          # implements TokenProvider (jjwt)
│       ├── UserPrincipal.java             # adapter domain.User -> Spring UserDetails
│       ├── UserDetailsServiceImpl.java
│       ├── JwtAuthenticationFilter.java
│       └── SecurityConfig.java
│
└── api/                 # REST — kontrolery, DTO wejścia/wyjścia, mapowanie błędów
    ├── ErrorResponse.java
    ├── GlobalExceptionHandler.java        # @RestControllerAdvice: wyjątki domenowe -> HTTP
    └── auth/
        ├── AuthController.java            # wywołuje use case'y, nic więcej
        └── RegisterRequest / LoginRequest / AuthResponse (walidacja @Valid tu, nie w domenie)
```

### Wzorce

- **Ports & Adapters (Hexagonal)** — `domain`/`application` nie znają Springa/JPA; wszystko przez interfejsy
- **Repository** — port w `domain`, adapter (`UserRepositoryAdapter`) w `infrastructure`
- **Use Case / Interactor** — jedna klasa = jedna operacja biznesowa (`RegisterUserUseCase`, `LoginUseCase`)
- **Strategy** — `PasswordHasher`, `TokenProvider` jako wymienne implementacje
- **Factory method** — `User.register(...)` pilnuje niezmienników zamiast publicznego settera
- **Centralny error handling** — `@RestControllerAdvice` zamiast obsługi błędów rozrzuconej po kontrolerach

## Encje

### User (`users`, `UserJpaEntity`)

| Pole | Typ | Uwagi |
|---|---|---|
| id | Long | PK, auto-increment |
| email | String | unique, not null |
| passwordHash | String | not null, hash BCrypt |
| firstName / lastName | String | not null |
| role | Role (enum) | EMPLOYEE / MANAGER / HR_ADMIN |
| createdAt | Instant | ustawiane w `@PrePersist` |

Domenowy `domain.user.User` jest niemutowalny i nie ma żadnych adnotacji —
`UserRepositoryAdapter` mapuje go do/z `UserJpaEntity` na granicy warstwy infrastructure.

## Moduł: Auth (JWT)

| Endpoint | Opis | Auth wymagane |
|---|---|---|
| `POST /api/auth/register` | Rejestruje nowego użytkownika (rola domyślna `EMPLOYEE`), zwraca JWT | Nie |
| `POST /api/auth/login` | Loguje po email+hasło, zwraca JWT | Nie |

Request/response:
```jsonc
// POST /api/auth/register
{ "email": "jan@example.com", "password": "min8znakow", "firstName": "Jan", "lastName": "Kowalski" }
// -> 201 { "token": "<jwt>" }  |  409 { "message": "..." } gdy e-mail zajęty

// POST /api/auth/login
{ "email": "jan@example.com", "password": "min8znakow" }
// -> 200 { "token": "<jwt>" }  |  401 { "message": "..." } gdy złe dane
```

Wszystkie pozostałe endpointy (`/api/**` poza `/api/auth/**`) wymagają nagłówka
`Authorization: Bearer <token>` — patrz `infrastructure/security/SecurityConfig.java`
i `JwtAuthenticationFilter.java`.

Hasła hashowane BCryptem (`BCryptPasswordHasher`), sesje bezstanowe (`STATELESS`).
Sekret JWT i czas wygaśnięcia konfigurowalne przez `app.jwt.secret` /
`app.jwt.expiration-ms` (env: `JWT_SECRET`).

Wspólny port `application/common/CurrentUserProvider.java` (implementacja:
`infrastructure/security/SecurityContextCurrentUserProvider.java`) daje use
case'om dostęp do id/e-maila/roli aktualnie zalogowanego użytkownika bez
zależności od Spring Security — używają go też moduły Leave i Time Tracking.

## Moduł: Leave Requests (wnioski urlopowe)

Tabela `leave_requests`. Typ urlopu jako enum (`VACATION`, `SICK_LEAVE`,
`UNPAID`, `OTHER`) — bez osobnej konfigurowalnej tabeli `leave_types` na tym
etapie (celowo odłożone, podobnie jak limity/salda urlopowe `LeaveBalance`).

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `POST /api/leave-requests` | Tworzy wniosek (status `PENDING`) | dowolny zalogowany |
| `GET /api/leave-requests/me` | Lista własnych wniosków | dowolny zalogowany |
| `GET /api/leave-requests/pending` | Lista wniosków `PENDING` | `MANAGER` / `HR_ADMIN` |
| `PATCH /api/leave-requests/{id}/approve` | Zatwierdza wniosek | `MANAGER` / `HR_ADMIN` |
| `PATCH /api/leave-requests/{id}/reject` | Odrzuca wniosek | `MANAGER` / `HR_ADMIN` |

```jsonc
// POST /api/leave-requests
{ "type": "VACATION", "startDate": "2026-08-03", "endDate": "2026-08-07", "reason": "Wakacje" }
// -> 201 { "id": 1, "requesterId": 5, "type": "VACATION", "startDate": "2026-08-03",
//          "endDate": "2026-08-07", "daysCount": 5, "status": "PENDING", ... }
```

Błędy: 400 (zły zakres dat / walidacja), 403 (rola bez uprawnień do approve/reject),
404 (brak wniosku), 409 (decyzja na wniosku, który nie jest już `PENDING`).
Mapowane przez dedykowany `api.leave.LeaveExceptionHandler` (nie w
`GlobalExceptionHandler`, żeby moduły mogły rozwijać się niezależnie).

## Moduł: Time Tracking (czas pracy)

Tabela `time_entries`. Bez raportów/agregacji miesięcznych na tym etapie —
tylko clock-in/clock-out i lista własnych wpisów.

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `POST /api/time-entries/clock-in` | Otwiera nowy wpis (błąd 409 jeśli już jest otwarty) | dowolny zalogowany |
| `POST /api/time-entries/clock-out` | Zamyka otwarty wpis (błąd 409 jeśli brak otwartego) | dowolny zalogowany |
| `GET /api/time-entries/me` | Lista własnych wpisów | dowolny zalogowany |

```jsonc
// POST /api/time-entries/clock-in
// -> 201 { "id": 1, "userId": 5, "clockIn": "...", "clockOut": null, "breakMinutes": 0, "totalMinutes": null }

// POST /api/time-entries/clock-out
// -> 200 { ..., "clockOut": "...", "totalMinutes": 480 }
```

Błędy mapowane przez dedykowany `api.timetracking.TimeTrackingExceptionHandler`
(analogicznie do modułu Leave — każdy moduł ma własny handler, żeby uniknąć
współdzielenia jednego dużego pliku między niezależnie rozwijanymi modułami).

## Testy

| Warstwa | Typ testu | Przykład |
|---|---|---|
| `domain` | Unit (czysty Java, bez Springa) | `LeaveRequestTest`, `TimeEntryTest` |
| `application` | Unit (Mockito, porty mockowane) | `RegisterUserUseCaseTest`, `CreateLeaveRequestUseCaseTest`, `ClockInUseCaseTest` |
| `infrastructure.persistence` | `@DataJpaTest` (H2) | `UserRepositoryAdapterTest`, `LeaveRequestRepositoryAdapterTest`, `TimeEntryRepositoryAdapterTest` |
| `infrastructure.security` | Unit (bez Springa) | `JwtTokenProviderTest` |
| `api` | `@SpringBootTest` + MockMvc (pełny kontekst) | `AuthControllerTest`, `LeaveRequestControllerTest`, `TimeEntryControllerTest` |

Uwaga: e-maile testowe w każdej klasie `@SpringBootTest` muszą być unikalne w
całym module — Spring cache'uje kontekst (i bazę H2) między klasami testów o
identycznej konfiguracji, więc powtórzony e-mail w dwóch klasach kończy się
kolizją unikalności (409) zamiast izolacji.

Uwaga testowa: rejestracja (`/api/auth/register`) zawsze tworzy użytkownika z
rolą `EMPLOYEE`. Żeby przetestować ścieżki wymagające `MANAGER`/`HR_ADMIN`,
testy integracyjne zapisują użytkownika bezpośrednio przez `UserRepository`
(`User.reconstitute(...)`) i generują token przez `TokenProvider` — z
pominięciem endpointu rejestracji.

## Moduły (w budowie)

| Moduł | Status |
|---|---|
| Bootstrap projektu | ✅ |
| Clean Architecture (domain/application/infrastructure/api) | ✅ |
| User + JWT auth (register/login) | ✅ |
| Leave requests | ✅ |
| Time tracking | ✅ |
