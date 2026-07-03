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

## Struktura

```
src/
├── api/
│   ├── client.ts      # fetch wrapper (JSON, Bearer token, mapowanie błędów)
│   ├── auth.ts         # register()/login()
│   └── types.ts        # DTO + ApiError
├── auth/
│   ├── AuthContext.tsx # token w localStorage, useAuth() hook
│   └── jwt.ts           # dekodowanie payloadu JWT (email z pola `sub`, sprawdzanie exp)
├── components/
│   └── ProtectedRoute.tsx
└── pages/
    ├── LoginPage.tsx
    ├── RegisterPage.tsx
    └── DashboardPage.tsx   # placeholder po zalogowaniu
```

## Zaimplementowane ekrany

| Ścieżka | Opis |
|---|---|
| `/login` | Logowanie (e-mail + hasło) → zapisuje JWT, przekierowuje do `/dashboard` |
| `/register` | Rejestracja (e-mail, hasło, imię, nazwisko) → zapisuje JWT, przekierowuje do `/dashboard` |
| `/dashboard` | Chronione przez `ProtectedRoute` — wymaga tokenu; pokazuje e-mail zalogowanego użytkownika, przycisk wylogowania |

Token JWT trzymany w `localStorage`, sprawdzany pod kątem wygaśnięcia przy
starcie aplikacji (`AuthContext`). Ekrany dla modułów Leave Requests i Time
Tracking nie są jeszcze zaimplementowane — tylko auth (zgodnie z aktualnym
zakresem).
