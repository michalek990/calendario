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

Domyślna kolorystyka: jasny niebieski (`--primary`) + biel + żółty
(`--yellow`) jako kolor kontrastu/akcentu. Navbar ma tło w kolorze wiodącym
z żółtym podkreśleniem aktywnej zakładki, karty formularzy mają żółty pasek
na górze. Wszystko konfigurowalne na `/settings`:
- **tryb ciemny** — przełącznik, zmienia `--bg`/`--surface`/`--text`/`--border`
- **rozmiar czcionki** — mała/średnia/duża, zmienia `font-size` na `<html>`
  (reszta aplikacji skalowana przez jednostki `rem`)
- **kolor wiodący** — wybór z 5 gotowych presetów (niebieski, granat,
  zieleń, fiolet, turkusowy), nadpisuje `--primary`/`--primary-light`/
  `--primary-lighter`

Ustawienia trzymane w `localStorage` (`src/settings/SettingsContext.tsx`),
stosowane jako atrybuty/zmienne CSS na `document.documentElement` — nie
wymagają przeładowania strony.

## Struktura

```
src/
├── api/
│   ├── client.ts        # fetch wrapper (GET/POST/PATCH, Bearer token, mapowanie błędów)
│   ├── auth.ts           # register()/login()/changePassword()
│   ├── leave.ts          # create/list/approve/reject wniosków urlopowych
│   ├── timeEntries.ts    # clockIn()/clockOut()/listMyTimeEntries()
│   └── types.ts          # DTO + ApiError
├── auth/
│   ├── AuthContext.tsx   # token w localStorage, email/rola/imię+nazwisko z payloadu JWT, hasAnyRole()
│   └── jwt.ts             # dekodowanie payloadu JWT (sub, role, firstName, lastName, exp)
├── settings/
│   ├── SettingsContext.tsx # tryb ciemny, rozmiar czcionki, kolor wiodący — trwałe w localStorage
│   └── colorPresets.ts     # 5 presetów koloru wiodącego + mapowanie rozmiarów czcionki na px
├── components/
│   ├── AppLayout.tsx        # nagłówek z nawigacją (linki zależne od roli) + UserAvatarMenu + wylogowanie
│   ├── UserAvatarMenu.tsx   # kółko z inicjałami w prawym górnym rogu — popup z danymi profilu (nie osobna strona)
│   ├── MonthCalendar.tsx    # siatka bieżącego miesiąca z oznaczonymi dniami
│   └── ProtectedRoute.tsx   # wymaga tokenu; opcjonalnie `allowedRoles`
├── utils/
│   └── calendar.ts        # generowanie siatki miesiąca, iteracja po zakresie dat ISO
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    ├── DashboardPage.tsx        # powitanie, rola, kalendarz miesiąca
    ├── LeaveRequestsPage.tsx    # wyśrodkowany formularz nowego wniosku (karta) + lista własnych poniżej
    ├── PendingApprovalsPage.tsx # lista PENDING + zatwierdź/odrzuć (MANAGER/HR_ADMIN)
    ├── TimeTrackingPage.tsx     # wyśrodkowana karta rejestracji czasu (rozpocznij/zakończ) + lista wpisów poniżej
    └── SettingsPage.tsx         # dark mode, rozmiar czcionki, kolor wiodący, zmiana hasła
```

## Zaimplementowane ekrany

| Ścieżka | Opis | Dostęp |
|---|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/dashboard` | Powitanie, rola, kalendarz bieżącego miesiąca z zaznaczonymi dniami urlopu/pracy | zalogowany |
| `/leave-requests` | Wyśrodkowany formularz nowego wniosku (karta) + lista własnych wniosków ze statusem poniżej | zalogowany |
| `/leave-requests/pending` | Tabela wniosków `PENDING` z akcjami zatwierdź/odrzuć | `MANAGER` / `HR_ADMIN` (link w nawigacji i `ProtectedRoute` ukryte/zablokowane dla innych ról) |
| `/time-tracking` | Wyśrodkowana karta rozpocznij/zakończ pracę + lista własnych wpisów poniżej | zalogowany |
| `/settings` | Dark mode, rozmiar czcionki, wybór koloru wiodącego, formularz zmiany hasła | zalogowany |

Profil użytkownika **nie jest osobną stroną** — to kółko z inicjałami w
prawym górnym rogu nagłówka (obok przycisku "Wyloguj się"); kliknięcie
otwiera popup z imieniem, nazwiskiem, e-mailem i rolą (`UserAvatarMenu.tsx`).

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Token niesie claimy `role`, `firstName`,
`lastName` obok `sub` (e-mail) — dodane po stronie backendu w
`JwtTokenProvider`, żeby frontend mógł pokazywać dane profilu i
pokazywać/ukrywać ekrany zależne od roli bez dodatkowego zapytania do API.

## Zmiana hasła

`/settings` zawiera formularz (obecne hasło, nowe hasło, powtórzenie) który
woła `PATCH /api/auth/change-password` (nowy endpoint backendu — patrz
[backend/README.md](../backend/README.md)). Walidacja "nowe hasło = powtórzenie"
robiona po stronie frontendu przed wysłaniem żądania.

## Kalendarz na pulpicie

`DashboardPage` pobiera równolegle własne wnioski urlopowe i wpisy czasu
pracy, po czym buduje mapę `dzień ISO -> status` dla bieżącego miesiąca:
- dni z jakimkolwiek wpisem `clock-in` → oznaczone jako "praca" (kolor wiodący)
- dni w zakresie zatwierdzonego (`APPROVED`) wniosku urlopowego → oznaczone
  jako "urlop" (żółte tło); wnioski `PENDING`/`REJECTED` nie są zaznaczane
`MonthCalendar` (współdzielony komponent, `utils/calendar.ts` generuje
siatkę tygodni) renderuje to jako siatkę 7-kolumnową z legendą kolorów i
wyróżnieniem dzisiejszej daty.

Uwaga: `PendingApprovalsPage` pokazuje wnioskodawcę jako `#{requesterId}` —
backend na razie nie zwraca w `LeaveRequestView` nazwiska/e-maila
wnioskującego (tylko jego id), więc frontend nie ma skąd wziąć więcej.
