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
│   ├── timeEntries.ts      # clockIn/clockOut/logTimeEntry/updateTimeEntry/listMyTimeEntries/listMyTimeByProject/listManagedTimeEntries
│   ├── users.ts            # getMyProfile/updateMyPersonalInfo/updateUserOrganization/listAllUsers/updateUserRole
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
│   ├── AppLayout.tsx            # sidebar (nawigacja zależna od roli, licznik nieprzeczytanych) + topbar + UserAvatarMenu
│   ├── icons.tsx                # zestaw ręcznie napisanych ikon SVG (bez zewnętrznej biblioteki)
│   ├── UserAvatarMenu.tsx       # kółko z inicjałami — popup z danymi profilu + link do `/profile`
│   ├── MonthCalendar.tsx        # siatka bieżącego miesiąca z oznaczonymi dniami
│   ├── LeaveApprovalTable.tsx   # tabela wniosków PENDING + zatwierdź/odrzuć — współdzielona przez TeamManagementPage i AdminFacilitiesPage
│   ├── ManagedTimeEntriesList.tsx # lista wpisów czasu pracy + edycja w miejscu — współdzielona przez TeamManagementPage i AdminFacilitiesPage
│   └── ProtectedRoute.tsx       # wymaga tokenu; opcjonalnie `allowedRoles`
├── utils/
│   └── calendar.ts        # generowanie siatki miesiąca, iteracja po zakresie dat ISO
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    ├── DashboardPage.tsx        # powitanie, rola, kalendarz miesiąca, ostatnie zmiany na wnioskach
    ├── LeaveRequestsPage.tsx    # podsumowanie roczne (praca zdalna vs reszta, limit 26 dni) + formularz nowego wniosku + lista własnych
    ├── TeamManagementPage.tsx   # zakładki: wnioski PENDING (zatwierdź/odrzuć) i czas pracy zespołu (podgląd + edycja) — MANAGER (tylko podwładni w module wniosków) / HR / ADMIN
    ├── TimeTrackingPage.tsx     # rozpocznij/zakończ pracę z wyborem projektu + własne podsumowanie per projekt + lista wpisów
    ├── ProjectsPage.tsx         # lista projektów, formularz nowego projektu (HR/ADMIN), własne i zespołowe podsumowania czasu
    ├── NotificationsPage.tsx    # lista powiadomień, oznacz jako przeczytane
    ├── ProfilePage.tsx          # własny profil (dane organizacyjne + personalne), edycja danych osobowych, HR/ADMIN: edycja danych organizacyjnych innego pracownika po id
    ├── SettingsPage.tsx         # dark mode, rozmiar czcionki, kolor wiodący, zmiana hasła
    ├── AdminUsersPage.tsx       # lista wszystkich pracowników (rola, stanowisko, dział, zakład, przełożony) — ADMIN
    ├── AdminUserEditPage.tsx    # osobny widok edycji jednego użytkownika (rola/stanowisko/dział/zakład/przełożony) — ADMIN
    └── AdminFacilitiesPage.tsx  # wybór zakładu + wnioski urlopowe i czas pracy tylko pracowników tego zakładu — ADMIN
```

## Zaimplementowane ekrany

| Ścieżka | Opis | Dostęp |
|---|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` | publiczny |
| `/dashboard` | Powitanie, rola, kalendarz bieżącego miesiąca, widget "ostatnie zmiany na wnioskach" | zalogowany |
| `/leave-requests` | Podsumowanie roczne (dni pracy zdalnej vs pozostałe, wykorzystanie limitu urlopu wypoczynkowego) + formularz nowego wniosku (wszystkie 10 typów) + lista własnych wniosków | zalogowany |
| `/team` | Zakładki: wnioski `PENDING` (zatwierdź/odrzuć) i czas pracy zespołu (podgląd + edycja wpisu) | `MANAGER` (backend dodatkowo ogranicza wnioski do bezpośrednich podwładnych) / `HR` / `ADMIN` |
| `/time-tracking` | Rozpocznij/zakończ pracę z opcjonalnym wyborem projektu + własne podsumowanie czasu per projekt + lista wpisów | zalogowany |
| `/projects` | Lista projektów; formularz nowego projektu; własne podsumowanie czasu per projekt; zbiorcze podsumowanie zespołu per projekt | zalogowany (tworzenie: `HR`/`ADMIN`; zbiorcze podsumowanie: `MANAGER`/`HR`/`ADMIN`) |
| `/notifications` | Lista powiadomień (decyzje na wnioskach), oznacz jako przeczytane | zalogowany |
| `/profile` | Własny profil: dane organizacyjne (stanowisko/dział/zakład/przełożony), dane personalne (data urodzenia/telefon/awatar), edycja danych personalnych; sekcja dodatkowa dla HR/ADMIN do edycji danych organizacyjnych innego pracownika po id | zalogowany (sekcja edycji innych: `HR`/`ADMIN`) |
| `/settings` | Dark mode, rozmiar czcionki, wybór koloru wiodącego, formularz zmiany hasła | zalogowany |
| `/admin/users` | Lista wszystkich pracowników (rola, stanowisko, dział, zakład, przełożony) — edycja przenosi na osobną podstronę zamiast edycji w wierszu tabeli | `ADMIN` |
| `/admin/users/:id` | Osobny widok edycji jednego pracownika: rola, stanowisko, dział, zakład, przełożony | `ADMIN` |
| `/admin/facilities` | Wybór zakładu z listy + wnioski urlopowe i czas pracy **tylko** pracowników wybranego zakładu (te same tabele co `/team`, dodatkowo z imieniem/nazwiskiem wnioskodawcy zamiast samego id) | `ADMIN` |

Kółko z inicjałami w prawym górnym rogu nagłówka nadal pokazuje szybki popup
(imię, nazwisko, e-mail, rola) i teraz linkuje do pełnej strony `/profile`
(`UserAvatarMenu.tsx`).

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Token niesie claimy `role`, `firstName`,
`lastName` obok `sub` (e-mail) — dodane po stronie backendu w
`JwtTokenProvider`, żeby frontend mógł pokazywać dane profilu i
pokazywać/ukrywać ekrany zależne od roli bez dodatkowego zapytania do API.

## Role (EMPLOYEE / MANAGER / HR / ADMIN)

Rejestracja (`/register`) zawsze tworzy `EMPLOYEE`. Zmianę roli istniejącego
pracownika robi `ADMIN` przez `/admin/users` → "Edytuj" → `/admin/users/:id`
(wywołuje `PATCH /api/users/{id}/role`) — nie trzeba już edytować bazy ręcznie.
Po zmianie roli użytkownik musi zalogować się ponownie, żeby dostać nowy JWT
(rola jest claimem ustalonym w momencie logowania, nie odświeża się w locie).

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

## Zarządzanie zespołem i zakładami

`LeaveApprovalTable` i `ManagedTimeEntriesList` (w `components/`) to
prezentacyjne komponenty współdzielone przez `TeamManagementPage` (`/team`) i
`AdminFacilitiesPage` (`/admin/facilities`) — logika zatwierdzania wniosków i
edycji wpisu czasu pracy żyje w jednym miejscu zamiast być zduplikowana.

Backend (`LeaveRequestView`) nie zwraca imienia/nazwiska wnioskodawcy, tylko
jego id, więc `LeaveApprovalTable` pokazuje wnioskodawcę jako `#{requesterId}`,
chyba że strona przekaże mapę `requesterNameById` (id → "Imię Nazwisko`).
`TeamManagementPage` tej mapy nie przekazuje — `MANAGER` nie ma dostępu do
`GET /api/users` (tylko `HR`/`ADMIN`), więc nie ma skąd jej wziąć. `AdminFacilitiesPage`
(tylko `ADMIN`, który ma dostęp do `GET /api/users`) buduje tę mapę z listy
wszystkich pracowników i pokazuje pełne imię i nazwisko zamiast samego id.

`AdminFacilitiesPage` filtruje wnioski i wpisy czasu pracy pobrane z
istniejących endpointów (`GET /api/leave-requests/pending`, `GET /api/time-entries`
— oba już zwracają dane wszystkich pracowników dla `ADMIN`) po polu
`facility` dopasowanym przez id użytkownika z `GET /api/users` — samo
filtrowanie po zakładzie dzieje się więc całkowicie po stronie frontendu, bez
zmian w API.
