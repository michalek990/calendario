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

Layout w stylu produktu HR SaaS: stały **sidebar po prawej stronie** (tło w
kolorze wiodącym, ikony + etykiety; aktywna pozycja to pełne białe wypełnienie
z tekstem w kolorze wiodącym — bez cienkiego "podkreślenia") i **topbar** nad
treścią z dzwonkiem powiadomień (z licznikiem nieprzeczytanych) i menu
awatara. Strony logowania/rejestracji mają dwupanelowy układ — jedna połowa
branding na gradiencie koloru wiodącego, druga formularz na jasnym tle.
Pulpit ma rząd kart KPI (`stat-card`): pozostały urlop, status pracy,
nieprzeczytane powiadomienia.

Ikony (`components/icons.tsx`) to odręcznie napisane komponenty SVG w stylu
Feather/Lucide (stroke, bez wypełnienia) — celowo bez zewnętrznej biblioteki
ikon, żeby nie dokładać zależności dla garści prostych glifów.

Typografia: **IBM Plex Sans** (Google Fonts, `index.html`) zamiast domyślnego
stosu systemowego — bardziej rozpoznawalny, "korporacyjny" krój niż
domyślny `system-ui`. Kształt: celowo **kwadratowy**, nie zaokrąglony —
`--radius-sm/md/lg` w `index.css` to 4/6/8px (zamiast poprzednich
8/12/18px), więc karty, odznaki, avatar i przełącznik koloru mają ledwo
zauważalne zaokrąglenie zamiast "bąbelkowego" SaaS-owego looku. Wyjątek:
przełącznik trybu ciemnego (`.switch`) został pigułką/kółkiem, bo to
rozpoznawalny wzorzec interakcji (iOS-owy toggle), nie ozdobnik.

Paleta: jaśniejszy, bardziej nasycony niebieski (`--primary: #2196f3`) + biel
+ żółty (`--yellow`) jako akcent, spójne tokeny odstępów/promieni/cieni
(`--radius-sm/md/lg`, `--shadow-sm/md`) w `index.css`. Każdy samodzielny
przycisk poza formularzem/tabelą (np. "Oznacz jako przeczytane",
"Pokaż podsumowanie zespołu") ma klasę `.btn-secondary` — wcześniej takie
przyciski nie miały żadnej klasy i renderowały się z domyślnym stylem
przeglądarki. Wszystko konfigurowalne na `/settings`:
- **tryb ciemny** — przełącznik, zmienia `--bg`/`--surface`/`--text`/`--border`
  (sidebar zostaje w kolorze wiodącym niezależnie od motywu — to stały
  element brandingu, tak jak w większości produktów SaaS)
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
│   ├── client.ts          # fetch wrapper (GET/POST/PATCH, Bearer token, mapowanie błędów)
│   ├── auth.ts             # register()/login()/changePassword()
│   ├── leave.ts            # create/list/approve/reject + recent-activity + annual-summary
│   ├── timeEntries.ts      # clockIn(projectId?)/clockOut()/listMyTimeEntries()/listMyTimeByProject()
│   ├── users.ts            # getMyProfile()/updateMyPersonalInfo()/updateUserOrganization()
│   ├── notifications.ts    # listMyNotifications()/markNotificationAsRead()
│   ├── projects.ts         # listProjects()/createProject()/getProjectTimeSummary()
│   └── types.ts            # DTO + ApiError
├── auth/
│   ├── AuthContext.tsx   # token w localStorage, email/rola/imię+nazwisko z payloadu JWT, hasAnyRole()
│   └── jwt.ts             # Role (EMPLOYEE/MANAGER/HR/ADMIN), dekodowanie JWT, ROLE_LABELS
├── constants/
│   └── labels.ts          # LEAVE_TYPE_LABELS / STATUS_LABELS / NOTIFICATION_TYPE_LABELS — współdzielone między stronami
├── settings/
│   ├── SettingsContext.tsx # tryb ciemny, rozmiar czcionki, kolor wiodący — trwałe w localStorage
│   └── colorPresets.ts     # 5 presetów koloru wiodącego + mapowanie rozmiarów czcionki na px
├── components/
│   ├── AppLayout.tsx        # sidebar (nawigacja zależna od roli, licznik nieprzeczytanych) + topbar + UserAvatarMenu
│   ├── icons.tsx            # zestaw ręcznie napisanych ikon SVG (bez zewnętrznej biblioteki)
│   ├── UserAvatarMenu.tsx   # kółko z inicjałami — popup z danymi profilu + link do `/profile`
│   ├── MonthCalendar.tsx    # siatka bieżącego miesiąca z oznaczonymi dniami
│   └── ProtectedRoute.tsx   # wymaga tokenu; opcjonalnie `allowedRoles`
├── utils/
│   └── calendar.ts        # generowanie siatki miesiąca, iteracja po zakresie dat ISO
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    ├── DashboardPage.tsx        # powitanie, rola, kalendarz miesiąca, ostatnie zmiany na wnioskach
    ├── LeaveRequestsPage.tsx    # podsumowanie roczne (praca zdalna vs reszta, limit 26 dni) + formularz nowego wniosku + lista własnych
    ├── PendingApprovalsPage.tsx # lista PENDING + zatwierdź/odrzuć (MANAGER — tylko podwładni / HR / ADMIN)
    ├── TimeTrackingPage.tsx     # rozpocznij/zakończ pracę z wyborem projektu + własne podsumowanie per projekt + lista wpisów
    ├── ProjectsPage.tsx         # lista projektów, formularz nowego projektu (HR/ADMIN), własne i zespołowe podsumowania czasu
    ├── NotificationsPage.tsx    # lista powiadomień, oznacz jako przeczytane
    ├── ProfilePage.tsx          # własny profil (dane organizacyjne + personalne), edycja danych osobowych, HR/ADMIN: edycja danych organizacyjnych innego pracownika po id
    └── SettingsPage.tsx         # dark mode, rozmiar czcionki, kolor wiodący, zmiana hasła
```

## Zaimplementowane ekrany

| Ścieżka | Opis | Dostęp |
|---|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/dashboard` | Powitanie, rola, kalendarz bieżącego miesiąca, widget "ostatnie zmiany na wnioskach" | zalogowany |
| `/leave-requests` | Podsumowanie roczne (dni pracy zdalnej vs pozostałe, wykorzystanie limitu urlopu wypoczynkowego) + formularz nowego wniosku (wszystkie 10 typów) + lista własnych wniosków | zalogowany |
| `/leave-requests/pending` | Tabela wniosków `PENDING` z akcjami zatwierdź/odrzuć | `MANAGER` (backend dodatkowo ogranicza do bezpośrednich podwładnych) / `HR` / `ADMIN` |
| `/time-tracking` | Rozpocznij/zakończ pracę z opcjonalnym wyborem projektu + własne podsumowanie czasu per projekt + lista wpisów | zalogowany |
| `/projects` | Lista projektów; formularz nowego projektu; własne podsumowanie czasu per projekt; zbiorcze podsumowanie zespołu per projekt | zalogowany (tworzenie: `HR`/`ADMIN`; zbiorcze podsumowanie: `MANAGER`/`HR`/`ADMIN`) |
| `/notifications` | Lista powiadomień (decyzje na wnioskach), oznacz jako przeczytane | zalogowany |
| `/profile` | Własny profil: dane organizacyjne (stanowisko/dział/zakład/przełożony), dane personalne (data urodzenia/telefon/awatar), edycja danych personalnych; sekcja dodatkowa dla HR/ADMIN do edycji danych organizacyjnych innego pracownika po id | zalogowany (sekcja edycji innych: `HR`/`ADMIN`) |
| `/settings` | Dark mode, rozmiar czcionki, wybór koloru wiodącego, formularz zmiany hasła | zalogowany |

Kółko z inicjałami w prawym górnym rogu nagłówka nadal pokazuje szybki popup
(imię, nazwisko, e-mail, rola) i teraz linkuje do pełnej strony `/profile`
(`UserAvatarMenu.tsx`).

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Token niesie claimy `role`, `firstName`,
`lastName` obok `sub` (e-mail) — dodane po stronie backendu w
`JwtTokenProvider`, żeby frontend mógł pokazywać dane profilu i
pokazywać/ukrywać ekrany zależne od roli bez dodatkowego zapytania do API.

## Role (EMPLOYEE / MANAGER / HR / ADMIN)

Frontend nie ma jeszcze ekranu do nadawania ról — rejestracja (`/register`)
zawsze tworzy `EMPLOYEE`. Żeby przetestować lokalnie ścieżki wymagające
`MANAGER`/`HR`/`ADMIN`, trzeba na razie zmienić rolę bezpośrednio w bazie
(`UPDATE users SET role = 'HR' WHERE email = '...'`), a potem zalogować się
ponownie (JWT niesie rolę jako claim ustalony w momencie logowania).

## Powiadomienia

`AppLayout` pobiera własne powiadomienia przy każdym wejściu do layoutu i
pokazuje licznik nieprzeczytanych przy linku "Powiadomienia" w nawigacji
(np. `Powiadomienia (2)`). Pełna lista i oznaczanie jako przeczytane są na
`/notifications` (`NotificationsPage.tsx`).

## Projekty i czas pracy w projekcie

`TimeTrackingPage` przy rozpoczęciu pracy pozwala wybrać projekt z listy
(`GET /api/projects`) — wybór jest opcjonalny, "Brak projektu" zostawia wpis
bez przypisania. Każda strona z podsumowaniem czasu w projekcie
(`TimeTrackingPage`, `ProjectsPage`) woła `GET /api/time-entries/me/by-project`
dla własnych danych; `ProjectsPage` dodatkowo, dla `MANAGER`/`HR`/`ADMIN`,
pozwala doładować zbiorcze podsumowanie całego zespołu per projekt
(`GET /api/projects/{id}/summary`) na żądanie (przycisk "Pokaż podsumowanie
zespołu"), żeby nie robić tego zapytania dla każdego projektu z góry.

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
