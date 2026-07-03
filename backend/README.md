# Backend — Calendario HR API

Spring Boot 3.3 / Java 21 / Maven.

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

## Struktura

```
backend/
├── src/main/java/com/calendario/hrnest/
│   └── HrnestApplication.java       # main class
├── src/main/resources/
│   └── application.properties       # config produkcyjny/dev (PostgreSQL)
├── src/main/java/com/calendario/hrnest/user/
│   ├── Role.java                     # enum: EMPLOYEE, MANAGER, HR_ADMIN
│   ├── User.java                     # encja JPA (tabela `users`)
│   └── UserRepository.java           # Spring Data repository
└── src/test/resources/
    └── application.properties       # config testowy (H2 in-memory)
```

## Encje

### User (`users`)

| Pole | Typ | Uwagi |
|---|---|---|
| id | Long | PK, auto-increment |
| email | String | unique, not null |
| passwordHash | String | not null (hasło haszowane przez BCrypt w kolejnym kroku) |
| firstName / lastName | String | not null |
| role | Role (enum) | EMPLOYEE / MANAGER / HR_ADMIN |
| createdAt | Instant | ustawiane automatycznie w `@PrePersist` |

`UserRepository.findByEmail(String)` i `existsByEmail(String)` — używane przez
moduł autoryzacji (rejestracja/logowanie).

## Moduł: Auth (JWT)

| Endpoint | Opis | Auth wymagane |
|---|---|---|
| `POST /api/auth/register` | Rejestruje nowego użytkownika (rola domyślna `EMPLOYEE`), zwraca JWT | Nie |
| `POST /api/auth/login` | Loguje po email+hasło, zwraca JWT | Nie |

Request/response:
```jsonc
// POST /api/auth/register
{ "email": "jan@example.com", "password": "min8znakow", "firstName": "Jan", "lastName": "Kowalski" }
// -> 201 { "token": "<jwt>" }

// POST /api/auth/login
{ "email": "jan@example.com", "password": "min8znakow" }
// -> 200 { "token": "<jwt>" }  |  401 gdy złe dane
```

Wszystkie pozostałe endpointy (`/api/**` poza `/api/auth/**`) wymagają nagłówka
`Authorization: Bearer <token>` — patrz `SecurityConfig` i `JwtAuthenticationFilter`.

Hasła hashowane BCryptem (`PasswordEncoder`), sesje bezstanowe (`STATELESS`).
Sekret JWT i czas wygaśnięcia konfigurowalne przez `app.jwt.secret` /
`app.jwt.expiration-ms` (env: `JWT_SECRET`).

## Moduły (w budowie)

| Moduł | Status |
|---|---|
| Bootstrap projektu | ✅ |
| User entity + repository | ✅ |
| JWT auth (register/login) | ✅ |
| Leave requests | ⏳ |
| Time tracking | ⏳ |
