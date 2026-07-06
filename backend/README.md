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
│   ├── user/
│   │   ├── User.java              # agregat, tworzony wyłącznie przez User.register(...) / reconstitute(...)
│   │   ├── Role.java              # enum: EMPLOYEE, MANAGER, HR, ADMIN
│   │   ├── UserRepository.java    # PORT — interfejs, bez śladu Spring Data
│   │   └── exception/             # EmailAlreadyExistsException, InvalidCredentialsException,
│   │                               # UserNotFoundException, ForbiddenUserActionException,
│   │                               # InvalidSupervisorAssignmentException, InvalidBirthDateException,
│   │                               # ForbiddenRoleChangeException
│   ├── notification/
│   │   ├── Notification.java          # agregat, powiadomienie w aplikacji
│   │   ├── NotificationType.java      # enum: LEAVE_REQUEST_APPROVED, LEAVE_REQUEST_REJECTED
│   │   ├── NotificationRepository.java # PORT
│   │   └── exception/                 # NotificationNotFoundException, ForbiddenNotificationActionException
│   ├── leave/
│   │   └── AnnualLeaveLimitPolicy.java # roczny limit urlopu wypoczynkowego (26 dni) — czysta logika domenowa
│   └── project/
│       ├── Project.java               # agregat: nazwa, opis
│       ├── ProjectRepository.java     # PORT
│       └── exception/                 # ProjectNotFoundException, DuplicateProjectNameException,
│                                       # ForbiddenProjectActionException, ForbiddenProjectSummaryAccessException
│
├── application/        # Use Case'y — orkiestracja, zależą TYLKO od domain
│   ├── auth/
│   │   ├── RegisterUserUseCase.java / LoginUseCase.java
│   │   ├── PasswordHasher.java    # PORT (strategy)
│   │   ├── TokenProvider.java     # PORT (strategy)
│   │   └── RegisterCommand / LoginCommand / AuthResult (DTO wewnętrzne use case'ów)
│   ├── notification/
│   │   ├── EmailSender.java               # PORT (strategy) — wysyłka e-maili
│   │   ├── LeaveDecisionNotifier.java      # powiadamia (w apce + mailem) o decyzji na wniosku
│   │   ├── ListMyNotificationsUseCase.java / MarkNotificationAsReadUseCase.java
│   │   └── NotificationView.java
│   └── project/
│       ├── CreateProjectUseCase.java / ListProjectsUseCase.java
│       ├── GetProjectTimeSummaryUseCase.java  # zbiorczy czas WSZYSTKICH pracowników na projekcie
│       └── ProjectView.java / ProjectTimeSummaryView.java
│
├── infrastructure/      # Adaptery — implementacje portów, szczegóły technologiczne
│   ├── persistence/
│   │   ├── UserJpaEntity.java             # encja JPA (tabela `users`)
│   │   ├── NotificationJpaEntity.java     # encja JPA (tabela `notifications`)
│   │   ├── ProjectJpaEntity.java          # encja JPA (tabela `projects`)
│   │   ├── SpringDataUserRepository.java  # Spring Data (package-private, szczegół implementacyjny)
│   │   └── UserRepositoryAdapter.java     # implements domain.user.UserRepository, mapuje JPA <-> domain
│   ├── security/
│   │   ├── BCryptPasswordHasher.java      # implements PasswordHasher
│   │   ├── JwtTokenProvider.java          # implements TokenProvider (jjwt)
│   │   ├── UserPrincipal.java             # adapter domain.User -> Spring UserDetails
│   │   ├── UserDetailsServiceImpl.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   └── email/
│       ├── LoggingEmailSender.java    # implements EmailSender — domyślna (dev/test), tylko loguje
│       └── SmtpEmailSender.java       # implements EmailSender — aktywna przy app.mail.enabled=true
│
└── api/                 # REST — kontrolery, DTO wejścia/wyjścia, mapowanie błędów
    ├── ErrorResponse.java
    ├── GlobalExceptionHandler.java        # @RestControllerAdvice: wyjątki domenowe -> HTTP
    ├── auth/
    │   ├── AuthController.java            # wywołuje use case'y, nic więcej
    │   └── RegisterRequest / LoginRequest / AuthResponse (walidacja @Valid tu, nie w domenie)
    ├── notification/
    │   ├── NotificationController.java
    │   └── NotificationExceptionHandler.java
    └── project/
        ├── ProjectController.java
        └── ProjectExceptionHandler.java
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
| role | Role (enum) | `EMPLOYEE` / `MANAGER` / `HR` / `ADMIN` — patrz niżej |
| position | String | stanowisko, nullable — uzupełniane przez HR/ADMIN po rejestracji |
| department | String | dział, nullable |
| facility | String | zakład / lokalizacja, nullable |
| supervisorId | Long | id przełożonego (`users.id`), nullable — brak = brak przełożonego |
| birthDate | LocalDate | data urodzenia, nullable — edytowalna samodzielnie przez użytkownika |
| phoneNumber | String | telefon kontaktowy, nullable — edytowalny samodzielnie |
| avatarUrl | String | URL do awatara (nie przechowujemy binarnie w bazie), nullable — edytowalny samodzielnie |
| lastLoginAt | Instant | znacznik ostatniego logowania, ustawiany automatycznie w `LoginUseCase` |
| createdAt | Instant | ustawiane w `@PrePersist` |

Domenowy `domain.user.User` jest niemutowalny i nie ma żadnych adnotacji —
`UserRepositoryAdapter` mapuje go do/z `UserJpaEntity` na granicy warstwy infrastructure.

**Role** (`Role` enum):

| Rola | Znaczenie |
|---|---|
| `EMPLOYEE` | Pracownik — składa wnioski, widzi tylko swoje dane |
| `MANAGER` | Przełożony — zatwierdza/odrzuca wnioski **swoich bezpośrednich podwładnych** (relacja `supervisorId`) |
| `HR` | Dział kadr — zatwierdza/odrzuca wnioski dowolnego pracownika, zarządza danymi organizacyjnymi |
| `ADMIN` | Administrator systemu — te same uprawnienia co `HR`, plus pełny dostęp do systemu |

Rejestracja (`/api/auth/register`) zawsze tworzy `EMPLOYEE`. Zmianę roli
istniejącego użytkownika obsługuje `PATCH /api/users/{id}/role` (wyłącznie
`ADMIN`) — patrz [Moduł: Profil użytkownika](#moduł-profil-użytkownika).

Dane organizacyjne (`position`/`department`/`facility`/`supervisorId`) są
`null` dla świeżo zarejestrowanego użytkownika — rejestracja zna tylko dane
logowania. Uzupełnia je HR/ADMIN przez `PATCH /api/users/{id}/profile`, dane
personalne (`birthDate`/`phoneNumber`/`avatarUrl`) użytkownik ustawia sam
przez `PATCH /api/users/me/personal-info` — patrz
[Moduł: Profil użytkownika](#moduł-profil-użytkownika).

Agregat pilnuje dwóch niezmienników: `User.updateOrganization(...)` rzuca
`InvalidSupervisorAssignmentException`, jeśli ktoś próbuje ustawić użytkownika
jako przełożonego samego siebie; `User.updatePersonalInfo(...)` rzuca
`InvalidBirthDateException`, jeśli data urodzenia wypada w przyszłości.

### Notification (`notifications`, `NotificationJpaEntity`)

| Pole | Typ | Uwagi |
|---|---|---|
| id | Long | PK, auto-increment |
| recipientId | Long | id odbiorcy (`users.id`), not null |
| type | NotificationType (enum) | `LEAVE_REQUEST_APPROVED` / `LEAVE_REQUEST_REJECTED` |
| message | String | gotowy tekst po polsku (np. "Twój wniosek (...) został zaakceptowany przez ...") |
| leaveRequestId | Long | id powiązanego wniosku, nullable |
| read | boolean | czy odbiorca oznaczył jako przeczytane |
| createdAt | Instant | ustawiane w `@PrePersist` |

Powiadomienia tworzone są wyłącznie wewnętrznie (przez `LeaveDecisionNotifier`
— patrz [Moduł: Powiadomienia](#moduł-powiadomienia)), nie ma endpointu do
ręcznego tworzenia.

## Moduł: Auth (JWT)

| Endpoint | Opis | Auth wymagane |
|---|---|---|
| `POST /api/auth/register` | Rejestruje nowego użytkownika (rola domyślna `EMPLOYEE`), zwraca JWT | Nie |
| `POST /api/auth/login` | Loguje po email+hasło, zwraca JWT | Nie |
| `PATCH /api/auth/change-password` | Zmienia hasło zalogowanego użytkownika | Tak |

Request/response:
```jsonc
// POST /api/auth/register
{ "email": "jan@example.com", "password": "min8znakow", "firstName": "Jan", "lastName": "Kowalski" }
// -> 201 { "token": "<jwt>" }  |  409 { "message": "..." } gdy e-mail zajęty

// POST /api/auth/login
{ "email": "jan@example.com", "password": "min8znakow" }
// -> 200 { "token": "<jwt>" }  |  401 { "message": "..." } gdy złe dane

// PATCH /api/auth/change-password (wymaga Authorization: Bearer <token>)
{ "currentPassword": "min8znakow", "newPassword": "noweMin8znakow" }
// -> 204 (brak treści)  |  401 { "message": "..." } gdy obecne hasło błędne
```

JWT (poza `sub` = e-mail) niesie też claimy `role`, `firstName`, `lastName` —
frontend używa ich do pokazywania profilu i ekranów zależnych od roli bez
dodatkowego zapytania do API.

Tylko `/api/auth/register` i `/api/auth/login` są publiczne (`permitAll`);
wszystkie pozostałe endpointy, w tym `/api/auth/change-password`, wymagają
nagłówka `Authorization: Bearer <token>` — patrz
`infrastructure/security/SecurityConfig.java` i `JwtAuthenticationFilter.java`.

Hasła hashowane BCryptem (`BCryptPasswordHasher`), sesje bezstanowe (`STATELESS`).
Sekret JWT i czas wygaśnięcia konfigurowalne przez `app.jwt.secret` /
`app.jwt.expiration-ms` (env: `JWT_SECRET`).

Wspólny port `application/common/CurrentUserProvider.java` (implementacja:
`infrastructure/security/SecurityContextCurrentUserProvider.java`) daje use
case'om dostęp do id/e-maila/roli aktualnie zalogowanego użytkownika bez
zależności od Spring Security — używają go też moduły Leave i Time Tracking.

## Moduł: Leave Requests (wnioski urlopowe)

Tabela `leave_requests`. Typ urlopu jako enum `LeaveType` — bez osobnej
konfigurowalnej tabeli `leave_types` na tym etapie (celowo odłożone, podobnie
jak limity/salda urlopowe `LeaveBalance`):

| Wartość enum | Wniosek |
|---|---|
| `VACATION` | Urlop wypoczynkowy |
| `ON_DEMAND` | Urlop na żądanie |
| `SICK_LEAVE` | Zwolnienie lekarskie |
| `UNPAID` | Urlop bezpłatny |
| `CHILDCARE_UNPAID` | Opieka nad dzieckiem — urlop bezpłatny |
| `OCCASIONAL` | Urlop okolicznościowy |
| `REMOTE_WORK` | Praca z domu / home office |
| `HOLIDAY_COMPENSATION` | Odbiór dnia wolnego za pracę w święto |
| `BUSINESS_TRIP` | Delegacja / podróż służbowa |
| `OTHER` | Inne |

Praca z domu (`REMOTE_WORK`) jest celowo modelowana jako `LeaveType`, nie
osobny agregat — z punktu widzenia procesu to też "wniosek o coś, wymagający
decyzji przełożonego", więc korzysta z tego samego cyklu życia
(`PENDING` → `APPROVED`/`REJECTED`) i tych samych endpointów.

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `POST /api/leave-requests` | Tworzy wniosek (status `PENDING`) | dowolny zalogowany |
| `GET /api/leave-requests/me` | Lista własnych wniosków | dowolny zalogowany |
| `GET /api/leave-requests/me/recent-activity` | Ostatnie zmiany na własnych wnioskach (max 10, sortowane po dacie ostatniej zmiany malejąco) | dowolny zalogowany |
| `GET /api/leave-requests/me/annual-summary` | Roczne podsumowanie: dni pracy zdalnej vs pozostałe nieobecności, wykorzystanie limitu urlopu wypoczynkowego | dowolny zalogowany |
| `GET /api/leave-requests/pending` | Lista wniosków `PENDING` | `MANAGER` (tylko bezpośredni podwładni) / `HR` / `ADMIN` |
| `PATCH /api/leave-requests/{id}/approve` | Zatwierdza wniosek | `MANAGER` (tylko bezpośredni podwładni) / `HR` / `ADMIN` |
| `PATCH /api/leave-requests/{id}/reject` | Odrzuca wniosek | `MANAGER` (tylko bezpośredni podwładni) / `HR` / `ADMIN` |

**Zakres uprawnień MANAGER.** Przełożony widzi w `/pending` i może
zatwierdzać/odrzucać wyłącznie wnioski użytkowników, których `supervisorId`
wskazuje na niego (bezpośredni podwładni) — sprawdzane przez
`LeaveRequestScopeGuard` we wszystkich trzech use case'ach. `HR` i `ADMIN` nie
podlegają temu ograniczeniu — widzą i decydują o wnioskach każdego pracownika.
Próba zatwierdzenia/odrzucenia wniosku spoza swojego zespołu przez `MANAGER`
kończy się 403, tak samo jak dla `EMPLOYEE`.

**Roczny limit urlopu wypoczynkowego (26 dni).** `AnnualLeaveLimitPolicy`
(czysta logika domenowa, bez zależności od repozytorium) pilnuje, żeby suma
dni `VACATION` + `ON_DEMAND` (urlop na żądanie prawnie jest częścią tej samej
puli — art. 167(2) KP) w jednym roku kalendarzowym nie przekroczyła 26 dni.
Do sumy liczą się wnioski `PENDING` i `APPROVED` (oczekujący wniosek też
"rezerwuje" pulę) — `REJECTED`/`CANCELLED` nie są brane pod uwagę. Rok wniosku
wyznacza `startDate`. Sprawdzane przez `CreateLeaveRequestUseCase` **przy
tworzeniu** wniosku — próba złożenia wniosku, który przekroczyłby limit,
kończy się 409 z `AnnualLeaveLimitExceededException` (komunikat zawiera ile
dni już wykorzystano i ile dodałby nowy wniosek). Limit jest współdzieloną
stałą (`AnnualLeaveLimitPolicy.ANNUAL_VACATION_LIMIT_DAYS`), nie polem
konfiguracyjnym — na tym etapie nie ma potrzeby różnicowania go per pracownik
(np. wg stażu pracy).

```jsonc
// POST /api/leave-requests
{ "type": "VACATION", "startDate": "2026-08-03", "endDate": "2026-08-07", "reason": "Wakacje" }
// -> 201 { "id": 1, "requesterId": 5, "type": "VACATION", "startDate": "2026-08-03",
//          "endDate": "2026-08-07", "daysCount": 5, "status": "PENDING", ... }
// -> 409 gdy suma dni VACATION+ON_DEMAND w danym roku przekroczyłaby 26

// GET /api/leave-requests/me/recent-activity
// -> 200 [ { "id": 3, "type": "REMOTE_WORK", "status": "APPROVED", ... }, { "id": 1, ... } ]
// (posortowane: approvedAt jeśli późniejszy niż createdAt, inaczej createdAt — malejąco)

// GET /api/leave-requests/me/annual-summary?year=2026 (year opcjonalny, domyślnie bieżący rok)
// -> 200 {
//   "year": 2026,
//   "daysByType": { "VACATION": 10, "REMOTE_WORK": 42 },
//   "remoteWorkDays": 42, "otherLeaveDays": 10,
//   "vacationDaysUsed": 10, "vacationDaysRemaining": 16, "vacationAnnualLimit": 26
// }
```

Podsumowanie roczne liczy tylko wnioski **`APPROVED`** (w przeciwieństwie do
limitu przy tworzeniu, który liczy też `PENDING`) — pokazuje faktycznie
zrealizowane dni, nie zarezerwowane. `remoteWorkDays` to dni `REMOTE_WORK`,
`otherLeaveDays` to suma wszystkich pozostałych typów — bezpośrednia
odpowiedź na "ile dni pracownik pracował zdalnie, a ile nie" w danym roku.

Błędy: 400 (zły zakres dat / walidacja), 403 (rola/zakres bez uprawnień do
approve/reject), 404 (brak wniosku), 409 (decyzja na wniosku, który nie jest
już `PENDING`, lub przekroczony roczny limit urlopu). Mapowane przez
dedykowany `api.leave.LeaveExceptionHandler` (nie w `GlobalExceptionHandler`,
żeby moduły mogły rozwijać się niezależnie).

Po zatwierdzeniu/odrzuceniu `ApproveLeaveRequestUseCase`/`RejectLeaveRequestUseCase`
wywołują `LeaveDecisionNotifier`, który tworzy powiadomienie w aplikacji i
wysyła e-mail do wnioskodawcy — patrz [Moduł: Powiadomienia](#moduł-powiadomienia).

## Moduł: Time Tracking (czas pracy)

Tabela `time_entries`. Bez raportów/agregacji miesięcznych na tym etapie —
clock-in/clock-out (na żywo albo "log" z ręcznie podanymi godzinami), lista
własnych wpisów, opcjonalne przypisanie wpisu do projektu, oraz — dla
HR/MANAGER/ADMIN — podgląd i korekta wpisów wszystkich pracowników.

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `POST /api/time-entries/clock-in` | Otwiera nowy wpis; opcjonalnie `projectId` i/lub `clockIn` (własna godzina rozpoczęcia zamiast "teraz") — błąd 409 jeśli już jest otwarty wpis, 404 jeśli projekt nie istnieje | dowolny zalogowany |
| `POST /api/time-entries/clock-out` | Zamyka otwarty wpis; opcjonalnie `clockOut` (własna godzina zakończenia) — błąd 409 jeśli brak otwartego wpisu | dowolny zalogowany |
| `POST /api/time-entries/log` | Rejestruje już zakończony wpis wprost (`clockIn`+`clockOut` obowiązkowe) — bez przechodzenia przez clock-in/clock-out "na żywo" | dowolny zalogowany |
| `PUT /api/time-entries/{id}` | Poprawia godziny/przerwę/projekt istniejącego wpisu | właściciel wpisu / `MANAGER` / `HR` / `ADMIN` |
| `GET /api/time-entries/me` | Lista własnych wpisów | dowolny zalogowany |
| `GET /api/time-entries/me/by-project` | Własny czas pracy zsumowany per projekt (tylko zamknięte wpisy) | dowolny zalogowany |
| `GET /api/time-entries` | Wpisy **wszystkich** pracowników, wzbogacone o imię/nazwisko/e-mail (`ManagedTimeEntryView`) — do zakładki zarządzania zespołem | `MANAGER` / `HR` / `ADMIN` (bez ograniczenia do bezpośrednich podwładnych, w przeciwieństwie do wniosków urlopowych) |

```jsonc
// POST /api/time-entries/clock-in
{ "projectId": 3, "clockIn": "2026-08-03T08:00:00Z" }   // całe ciało opcjonalne
// -> 201 { "id": 1, "userId": 5, "clockIn": "...", "clockOut": null, "breakMinutes": 0,
//          "totalMinutes": null, "projectId": 3 }
// -> 404 gdy projectId nie istnieje

// POST /api/time-entries/clock-out
// -> 200 { ..., "clockOut": "...", "totalMinutes": 480 }

// POST /api/time-entries/log
{ "clockIn": "2026-08-03T08:00:00Z", "clockOut": "2026-08-03T16:00:00Z", "breakMinutes": 30, "projectId": 3 }
// -> 201 <TimeEntryView>  |  400 gdy clockOut nie jest po clockIn  |  404 gdy projectId nie istnieje

// PUT /api/time-entries/5
{ "clockIn": "2026-08-03T08:00:00Z", "clockOut": "2026-08-03T15:30:00Z", "breakMinutes": 30, "projectId": null }
// -> 200 <TimeEntryView>  |  400 gdy clockOut nie jest po clockIn  |  404 gdy wpis lub projekt nie istnieje
//    403 gdy wywołujący nie jest właścicielem wpisu ani MANAGER/HR/ADMIN

// GET /api/time-entries/me/by-project
// -> 200 [ { "projectId": 3, "projectName": "Kalendario", "totalMinutes": 960, "entryCount": 2 } ]

// GET /api/time-entries (wymaga MANAGER/HR/ADMIN)
// -> 200 [ { "id": 5, "userId": 8, "userFirstName": "Jan", "userLastName": "Kowalski",
//            "userEmail": "jan@example.com", "clockIn": "...", "clockOut": "...", ... } ]
```

Błędy mapowane przez dedykowany `api.timetracking.TimeTrackingExceptionHandler`
(analogicznie do modułu Leave — każdy moduł ma własny handler, żeby uniknąć
współdzielenia jednego dużego pliku między niezależnie rozwijanymi modułami);
`ProjectNotFoundException` z clock-in/log/update jest jednak mapowany przez
`api.project.ProjectExceptionHandler` (jeden `@RestControllerAdvice` obsługuje
dany wyjątek niezależnie od tego, który kontroler go rzucił).

**Uwaga o zakresie `GET /api/time-entries`.** W przeciwieństwie do wniosków
urlopowych (gdzie `MANAGER` widzi tylko bezpośrednich podwładnych — patrz
[Moduł: Leave Requests](#moduł-leave-requests-wnioski-urlopowe)),
`ListManagedTimeEntriesUseCase` traktuje `MANAGER`/`HR`/`ADMIN` jednakowo i
zwraca wpisy wszystkich pracowników — filtrowanie po zakładzie/zespole robi
frontend (`AdminFacilitiesPage`), nie backend.

## Moduł: Profil użytkownika

Rozszerza `User` o dane organizacyjne (stanowisko, dział, zakład, przełożony)
i personalne (data urodzenia, telefon, awatar, ostatnie logowanie). Rozdzielone
celowo na dwa endpointy z różną autoryzacją — dane organizacyjne są
"twardą" strukturą firmy (edytuje HR/ADMIN), dane personalne należą do
użytkownika (edytuje sam siebie). "Czy jestem przełożonym" nie jest osobnym
polem — liczone dynamicznie jako "czy istnieje użytkownik, którego
`supervisorId` wskazuje na mnie" (`UserRepository.existsBySupervisorId`), żeby
nie trzeba było synchronizować dwóch źródeł prawdy przy zmianie struktury
podległości.

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `GET /api/users/me/profile` | Pełny profil zalogowanego użytkownika: dane organizacyjne, przełożony (id + imię i nazwisko), czy sam jest przełożonym, dane personalne, ostatnie logowanie | dowolny zalogowany |
| `GET /api/users` | Lista wszystkich użytkowników (pełny profil każdego, posortowana po nazwisku/imieniu) — do panelu administracyjnego | `HR` / `ADMIN` |
| `PATCH /api/users/{id}/profile` | Ustawia stanowisko/dział/zakład/przełożonego wskazanego użytkownika | `HR` / `ADMIN` |
| `PATCH /api/users/{id}/role` | Zmienia rolę wskazanego użytkownika (`EMPLOYEE`/`MANAGER`/`HR`/`ADMIN`) | `ADMIN` |
| `PATCH /api/users/me/personal-info` | Ustawia własną datę urodzenia/telefon/awatar | dowolny zalogowany (tylko dla siebie) |

```jsonc
// GET /api/users/me/profile
// -> 200 {
//   "id": 5, "email": "jan@example.com", "firstName": "Jan", "lastName": "Kowalski",
//   "role": "EMPLOYEE", "position": "Programista", "department": "IT", "facility": "Warszawa",
//   "isSupervisor": false, "hasSupervisor": true, "supervisorId": 2, "supervisorFullName": "Ala Szefowa",
//   "birthDate": "1990-05-01", "phoneNumber": "+48123456789", "avatarUrl": "https://.../a.png",
//   "lastLoginAt": "2026-07-06T08:00:00Z"
// }

// GET /api/users (wymaga HR lub ADMIN)
// -> 200 [ <UserProfileView>, <UserProfileView>, ... ]  |  403 gdy wywołujący nie jest HR/ADMIN

// PATCH /api/users/{id}/profile (wymaga HR lub ADMIN)
{ "position": "Programista", "department": "IT", "facility": "Warszawa", "supervisorId": 2 }
// -> 200 <UserProfileView>  |  400 gdy supervisorId == id (nie można być swoim przełożonym)
//    403 gdy wywołujący nie jest HR/ADMIN  |  404 gdy użytkownik lub przełożony nie istnieje

// PATCH /api/users/{id}/role (wymaga ADMIN)
{ "role": "MANAGER" }
// -> 200 <UserProfileView>  |  403 gdy wywołujący nie jest ADMIN  |  404 gdy użytkownik nie istnieje

// PATCH /api/users/me/personal-info (dowolny zalogowany, tylko własne dane)
{ "birthDate": "1990-05-01", "phoneNumber": "+48123456789", "avatarUrl": "https://.../a.png" }
// -> 200 <UserProfileView>  |  400 gdy birthDate w przyszłości
```

`PATCH /api/users/{id}/profile` i `PATCH /api/users/{id}/role` nadpisują swój
zestaw pól naraz, nie merguje się częściowo — np. `supervisorId: null` jawnie
usuwa przełożonego. Zmiana roli jest celowo osobnym, węższym endpointem
(tylko `ADMIN`, nie `HR`) — bardziej wrażliwa operacja niż dane organizacyjne,
stąd `ForbiddenRoleChangeException` zamiast dzielenia wyjątku z resztą
profilu. Błędy mapowane w `GlobalExceptionHandler` (ten moduł nie ma własnego
handlera — analogicznie do `EmailAlreadyExistsException`/`InvalidCredentialsException`,
wyjątki `domain.user.exception` trafiają tam).

## Moduł: Powiadomienia

Tabela `notifications`. Powiadamia wnioskodawcę o decyzji na jego wniosku —
zarówno w aplikacji, jak i mailem. Wyzwalane wyłącznie wewnętrznie przez
`LeaveDecisionNotifier` po zatwierdzeniu/odrzuceniu wniosku (patrz
[Moduł: Leave Requests](#moduł-leave-requests-wnioski-urlopowe)) — brak
endpointu do ręcznego tworzenia powiadomień.

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `GET /api/notifications/me` | Lista własnych powiadomień, najnowsze pierwsze | dowolny zalogowany |
| `PATCH /api/notifications/{id}/read` | Oznacza powiadomienie jako przeczytane | dowolny zalogowany (tylko własne) |

```jsonc
// GET /api/notifications/me
// -> 200 [{ "id": 1, "type": "LEAVE_REQUEST_APPROVED",
//           "message": "Twój wniosek (urlop wypoczynkowy, 2026-08-03 – 2026-08-07) został zaakceptowany przez Ala Szefowa.",
//           "leaveRequestId": 7, "read": false, "createdAt": "..." }]

// PATCH /api/notifications/{id}/read
// -> 200 <NotificationView z read=true>  |  403 gdy powiadomienie należy do innego użytkownika  |  404 gdy nie istnieje
```

Błędy mapowane przez dedykowany `api.notification.NotificationExceptionHandler`
(ta sama konwencja co Leave/Time Tracking).

## Moduł: E-mail

Wysyłka e-maili (na razie tylko decyzje o wnioskach) jest za portem
`application.notification.EmailSender`, z dwiema implementacjami dobieranymi
przez `@ConditionalOnProperty` na `app.mail.enabled`:

| Implementacja | Aktywna, gdy | Zachowanie |
|---|---|---|
| `LoggingEmailSender` | `app.mail.enabled=false` (**domyślnie**, w tym w testach) | Nie wysyła nic, tylko loguje treść — bezpieczne bez skonfigurowanego SMTP |
| `SmtpEmailSender` | `app.mail.enabled=true` | Wysyła realnie przez `JavaMailSender` (`spring-boot-starter-mail`) |

Konfiguracja produkcyjna (env): `MAIL_ENABLED=true`, `MAIL_HOST`, `MAIL_PORT`,
`MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` — patrz
`src/main/resources/application.properties`. Domyślnie wyłączona celowo, żeby
żadne środowisko (w tym dev/test) nie wysyłało maili przez przypadek, dopóki
ktoś świadomie nie skonfiguruje SMTP w produkcji.

## Moduł: Projekty

Tabela `projects` (id, `name` unique, `description`, `createdAt`). Projekty są
lekkim, płaskim słownikiem — bez statusu aktywny/nieaktywny czy hierarchii na
tym etapie (celowo odłożone, podobnie jak `LeaveBalance`). Każdy `TimeEntry`
może być opcjonalnie przypisany do projektu (`projectId`, nullable) —
przypisanie weryfikowane przy clock-in (patrz
[Moduł: Time Tracking](#moduł-time-tracking-czas-pracy)).

| Endpoint | Opis | Uprawnienia |
|---|---|---|
| `POST /api/projects` | Tworzy projekt (nazwa musi być unikalna) | `HR` / `ADMIN` |
| `GET /api/projects` | Lista wszystkich projektów, alfabetycznie | dowolny zalogowany (żeby wybrać projekt przy clock-in) |
| `GET /api/projects/{id}/summary` | Zbiorczy czas pracy **wszystkich** pracowników nad projektem (tylko zamknięte wpisy) | `MANAGER` / `HR` / `ADMIN` |

```jsonc
// POST /api/projects (wymaga HR lub ADMIN)
{ "name": "Kalendario", "description": "Aplikacja HR" }
// -> 201 { "id": 1, "name": "Kalendario", "description": "Aplikacja HR", "createdAt": "..." }
// -> 409 gdy nazwa już istnieje  |  403 gdy wywołujący nie jest HR/ADMIN

// GET /api/projects/1/summary
// -> 200 { "projectId": 1, "projectName": "Kalendario", "totalMinutes": 960, "entryCount": 2 }
// -> 403 gdy wywołujący jest zwykłym EMPLOYEE  |  404 gdy projekt nie istnieje
```

Rozróżnienie uprawnień: tworzenie projektu to zmiana struktury organizacyjnej
(`HR`/`ADMIN`, tak jak dane organizacyjne użytkownika), natomiast wgląd w
zbiorczy czas pracy nad projektem to też coś, czego potrzebuje `MANAGER` do
oceny obciążenia swojego zespołu — stąd szerszy dostęp do `/summary` niż do
tworzenia. Osobisty rozkład czasu per projekt (`GET /api/time-entries/me/by-project`)
jest dostępny dla każdego, bo dotyczy tylko własnych danych.

Błędy mapowane przez dedykowany `api.project.ProjectExceptionHandler`.

## Testy

| Warstwa | Typ testu | Przykład |
|---|---|---|
| `domain` | Unit (czysty Java, bez Springa) | `LeaveRequestTest`, `TimeEntryTest`, `UserTest`, `NotificationTest`, `ProjectTest`, `AnnualLeaveLimitPolicyTest` |
| `application` | Unit (Mockito, porty mockowane) | `RegisterUserUseCaseTest`, `CreateLeaveRequestUseCaseTest`, `ClockInUseCaseTest`, `ClockOutUseCaseTest`, `ListMyLeaveRequestsUseCaseTest`, `ListMyTimeEntriesUseCaseTest`, `ListRecentLeaveActivityUseCaseTest`, `ListPendingLeaveRequestsUseCaseTest`, `ListManagedTimeEntriesUseCaseTest`, `LogTimeEntryUseCaseTest`, `UpdateTimeEntryUseCaseTest`, `GetAnnualLeaveSummaryUseCaseTest`, `GetMyProfileUseCaseTest`, `ListAllUsersUseCaseTest`, `UpdateUserOrganizationUseCaseTest`, `UpdateUserRoleUseCaseTest`, `UpdateMyPersonalInfoUseCaseTest`, `ListMyNotificationsUseCaseTest`, `MarkNotificationAsReadUseCaseTest`, `LeaveDecisionNotifierTest`, `CreateProjectUseCaseTest`, `ListProjectsUseCaseTest`, `GetProjectTimeSummaryUseCaseTest`, `ListMyTimeByProjectUseCaseTest` |
| `infrastructure.persistence` | `@DataJpaTest` (H2) | `UserRepositoryAdapterTest`, `LeaveRequestRepositoryAdapterTest`, `TimeEntryRepositoryAdapterTest`, `NotificationRepositoryAdapterTest`, `ProjectRepositoryAdapterTest` |
| `infrastructure.security` | Unit (bez Springa) | `JwtTokenProviderTest` |
| `infrastructure.email` | Unit (bez Springa) | `LoggingEmailSenderTest` |
| `api` | `@SpringBootTest` + MockMvc (pełny kontekst) | `AuthControllerTest`, `LeaveRequestControllerTest`, `TimeEntryControllerTest`, `UserControllerTest`, `NotificationControllerTest`, `ProjectControllerTest` |

Uwaga: e-maile testowe w każdej klasie `@SpringBootTest` muszą być unikalne w
całym module — Spring cache'uje kontekst (i bazę H2) między klasami testów o
identycznej konfiguracji, więc powtórzony e-mail w dwóch klasach kończy się
kolizją unikalności (409) zamiast izolacji.

Uwaga testowa: rejestracja (`/api/auth/register`) zawsze tworzy użytkownika z
rolą `EMPLOYEE`. Żeby przetestować ścieżki wymagające `MANAGER`/`HR`/`ADMIN`,
testy integracyjne zapisują użytkownika bezpośrednio przez `UserRepository`
(`User.reconstitute(...)`) i generują token przez `TokenProvider` — z
pominięciem endpointu rejestracji.

## Moduły (w budowie)

| Moduł | Status |
|---|---|
| Bootstrap projektu | ✅ |
| Clean Architecture (domain/application/infrastructure/api) | ✅ |
| User + JWT auth (register/login) | ✅ |
| Role EMPLOYEE / MANAGER / HR / ADMIN | ✅ |
| Leave requests (w tym praca z domu, urlop na żądanie, okolicznościowy, opieka nad dzieckiem bezpłatna, odbiór za święto, delegacja) | ✅ |
| Zatwierdzanie wniosków — MANAGER tylko bezpośredni podwładni, HR/ADMIN każdy | ✅ |
| Roczny limit urlopu wypoczynkowego (26 dni, VACATION+ON_DEMAND) | ✅ |
| Roczne podsumowanie: praca zdalna vs pozostałe nieobecności (`/api/leave-requests/me/annual-summary`) | ✅ |
| Ostatnie zmiany na wnioskach (`/api/leave-requests/me/recent-activity`) | ✅ |
| Time tracking (clock-in/out, log ręczny, korekta wpisu, podgląd zespołu) | ✅ |
| Projekty + rejestrowanie czasu w projekcie + podsumowania (własne i zbiorcze) | ✅ |
| Profil użytkownika — dane organizacyjne (stanowisko, dział, zakład, przełożony) | ✅ |
| Profil użytkownika — dane personalne (data urodzenia, telefon, awatar, ostatnie logowanie) | ✅ |
| Lista wszystkich użytkowników i zmiana roli (`GET /api/users`, `PATCH /api/users/{id}/role`) | ✅ |
| Powiadomienia w aplikacji o decyzji na wniosku | ✅ |
| Powiadomienia mailowe o decyzji na wniosku (feature-flag `app.mail.enabled`) | ✅ |
