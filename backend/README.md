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

## Testy

| Warstwa | Typ testu | Przykład |
|---|---|---|
| `application` | Unit (Mockito, porty mockowane) | `RegisterUserUseCaseTest`, `LoginUseCaseTest` |
| `infrastructure.persistence` | `@DataJpaTest` (H2) | `UserRepositoryAdapterTest` |
| `infrastructure.security` | Unit (bez Springa) | `JwtTokenProviderTest` |
| `api` | `@SpringBootTest` + MockMvc (pełny kontekst) | `AuthControllerTest` |

## Moduły (w budowie)

| Moduł | Status |
|---|---|
| Bootstrap projektu | ✅ |
| Clean Architecture (domain/application/infrastructure/api) | ✅ |
| User + JWT auth (register/login) | ✅ |
| Leave requests | ⏳ |
| Time tracking | ⏳ |
