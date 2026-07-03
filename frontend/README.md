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

## Wygląd

Kolorystyka: granat (`--navy`) + biel + żółty (`--yellow`) jako kolor
akcentu/kontrastu. Navbar ma granatowe tło z żółtym podkreśleniem aktywnej
zakładki, karty logowania/rejestracji mają żółty pasek na górze. Zmienne
kolorów w `src/index.css`.

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
│   ├── AuthContext.tsx   # token w localStorage, email/rola/imię+nazwisko z payloadu JWT, hasAnyRole()
│   └── jwt.ts             # dekodowanie payloadu JWT (sub, role, firstName, lastName, exp)
├── components/
│   ├── AppLayout.tsx      # nagłówek z nawigacją (linki zależne od roli) + wylogowanie
│   ├── MonthCalendar.tsx  # siatka bieżącego miesiąca z oznaczonymi dniami
│   └── ProtectedRoute.tsx # wymaga tokenu; opcjonalnie `allowedRoles`
├── utils/
│   └── calendar.ts        # generowanie siatki miesiąca, iteracja po zakresie dat ISO
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    ├── DashboardPage.tsx        # powitanie, rola, kalendarz miesiąca
    ├── LeaveRequestsPage.tsx    # formularz nowego wniosku + lista własnych
    ├── PendingApprovalsPage.tsx # lista PENDING + zatwierdź/odrzuć (MANAGER/HR_ADMIN)
    ├── TimeTrackingPage.tsx     # clock-in/clock-out + lista własnych wpisów
    ├── ProfilePage.tsx          # dane konta (imię, nazwisko, e-mail, rola) — tylko odczyt
    └── SettingsPage.tsx         # placeholder — brak endpointów ustawień na backendzie
```

## Zaimplementowane ekrany

| Ścieżka | Opis | Dostęp |
|---|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/dashboard` | Powitanie, rola, kalendarz bieżącego miesiąca z zaznaczonymi dniami urlopu/pracy | zalogowany |
| `/leave-requests` | Formularz nowego wniosku urlopowego + tabela własnych wniosków ze statusem | zalogowany |
| `/leave-requests/pending` | Tabela wniosków `PENDING` z akcjami zatwierdź/odrzuć | `MANAGER` / `HR_ADMIN` (link w nawigacji i `ProtectedRoute` ukryte/zablokowane dla innych ról) |
| `/time-tracking` | Przycisk rozpocznij/zakończ pracę (zależny od tego, czy jest otwarty wpis) + tabela własnych wpisów | zalogowany |
| `/profile` | Dane konta — imię, nazwisko, e-mail, rola (tylko odczyt) | zalogowany |
| `/settings` | Placeholder — komunikat, że ustawień jeszcze nie ma w API | zalogowany |

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Token niesie claimy `role`, `firstName`,
`lastName` obok `sub` (e-mail) — dodane po stronie backendu w
`JwtTokenProvider`, żeby frontend mógł pokazywać dane profilu i
pokazywać/ukrywać ekrany zależne od roli bez dodatkowego zapytania do API.

## Kalendarz na pulpicie

`DashboardPage` pobiera równolegle własne wnioski urlopowe i wpisy czasu
pracy, po czym buduje mapę `dzień ISO -> status` dla bieżącego miesiąca:
- dni z jakimkolwiek wpisem `clock-in` → oznaczone jako "praca" (granatowe tło)
- dni w zakresie zatwierdzonego (`APPROVED`) wniosku urlopowego → oznaczone
  jako "urlop" (żółte tło); wnioski `PENDING`/`REJECTED` nie są zaznaczane
`MonthCalendar` (współdzielony komponent, `utils/calendar.ts` generuje
siatkę tygodni) renderuje to jako siatkę 7-kolumnową z legendą kolorów i
wyróżnieniem dzisiejszej daty.

Uwaga: `PendingApprovalsPage` pokazuje wnioskodawcę jako `#{requesterId}` —
backend na razie nie zwraca w `LeaveRequestView` nazwiska/e-maila
wnioskującego (tylko jego id), więc frontend nie ma skąd wziąć więcej.
