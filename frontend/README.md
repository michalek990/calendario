# Frontend — Calendario HR

React 19 + Vite + TypeScript (SPA). Konsumuje API backendu Spring Boot
(`../backend`) — patrz [backend/README.md](../backend/README.md) za dokumentacją endpointów.

## Uruchomienie

```bash
npm install
npm run dev
```

Dev server domyślnie na `http://localhost:5173` (Vite automatycznie wybierze
kolejny wolny port, jeśli 5173 jest zajęty przez inny proces). `/api/**` jest
proxowane do `http://localhost:8080` (backend) — patrz `vite.config.ts`.
Przez Docker Compose to samo proxowanie robi nginx (`nginx.conf`) w obrazie
produkcyjnym — patrz [root README](../README.md#uruchomienie-przez-docker-compose).

## Struktura

```
src/
├── api/
│   ├── client.ts        # fetch wrapper (GET/POST/PATCH, Bearer token, mapowanie błędów)
│   ├── auth.ts           # register()/login()
│   ├── leave.ts          # create/list/approve/reject wniosków urlopowych
│   ├── timeEntries.ts    # clockIn()/clockOut()/listMyTimeEntries()
│   └── types.ts          # DTO + ApiError
├── auth/
│   ├── AuthContext.tsx   # token w localStorage, email/rola z payloadu JWT, hasAnyRole()
│   └── jwt.ts             # dekodowanie payloadu JWT (sub, role, exp)
├── components/
│   ├── AppLayout.tsx      # nagłówek z nawigacją (linki zależne od roli) + wylogowanie
│   └── ProtectedRoute.tsx # wymaga tokenu; opcjonalnie `allowedRoles`
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    ├── DashboardPage.tsx        # powitanie + rola
    ├── LeaveRequestsPage.tsx    # formularz nowego wniosku + lista własnych
    ├── PendingApprovalsPage.tsx # lista PENDING + zatwierdź/odrzuć (MANAGER/HR_ADMIN)
    └── TimeTrackingPage.tsx     # clock-in/clock-out + lista własnych wpisów
```

## Zaimplementowane ekrany

| Ścieżka | Opis | Dostęp |
|---|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/dashboard` | Powitanie, pokazuje e-mail i rolę zalogowanego użytkownika | zalogowany |
| `/leave-requests` | Formularz nowego wniosku urlopowego + tabela własnych wniosków ze statusem | zalogowany |
| `/leave-requests/pending` | Tabela wniosków `PENDING` z akcjami zatwierdź/odrzuć | `MANAGER` / `HR_ADMIN` (link w nawigacji i `ProtectedRoute` ukryte/zablokowane dla innych ról) |
| `/time-tracking` | Przycisk rozpocznij/zakończ pracę (zależny od tego, czy jest otwarty wpis) + tabela własnych wpisów | zalogowany |

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Token od 2026-07-03 niesie też claim
`role` (dodane po stronie backendu w `JwtTokenProvider`), żeby frontend mógł
pokazywać/ukrywać ekrany zależne od roli bez dodatkowego zapytania do API.

Uwaga: `PendingApprovalsPage` pokazuje wnioskodawcę jako `#{requesterId}` —
backend na razie nie zwraca w `LeaveRequestView` nazwiska/e-maila
wnioskującego (tylko jego id), więc frontend nie ma skąd wziąć więcej.
