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
└── src/test/resources/
    └── application.properties       # config testowy (H2 in-memory)
```

## Moduły (w budowie)

| Moduł | Status |
|---|---|
| Bootstrap projektu | ✅ |
| User entity + repository | 🔜 |
| JWT auth (register/login) | 🔜 |
| Leave requests | ⏳ |
| Time tracking | ⏳ |
