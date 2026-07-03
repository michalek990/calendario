import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { UserAvatarMenu } from './UserAvatarMenu'

export function AppLayout({ children }: { children: ReactNode }) {
  const { logout, hasAnyRole } = useAuth()

  return (
    <div className="app-layout">
      <header className="app-header">
        <span className="app-brand">Calendario HR</span>
        <nav>
          <NavLink to="/dashboard">Pulpit</NavLink>
          <NavLink to="/time-tracking">Rejestracja czasu pracy</NavLink>
          <NavLink to="/leave-requests">Wnioski urlopowe</NavLink>
          {hasAnyRole('MANAGER', 'HR_ADMIN') && (
            <NavLink to="/leave-requests/pending">Do zatwierdzenia</NavLink>
          )}
          <NavLink to="/settings">Ustawienia</NavLink>
        </nav>
        <div className="app-header-user">
          <UserAvatarMenu />
          <button onClick={logout}>Wyloguj się</button>
        </div>
      </header>
      <main className="app-content">{children}</main>
    </div>
  )
}
