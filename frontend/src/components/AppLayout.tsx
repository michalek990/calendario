import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppLayout({ children }: { children: ReactNode }) {
  const { email, logout, hasAnyRole } = useAuth()

  return (
    <div className="app-layout">
      <header className="app-header">
        <nav>
          <NavLink to="/dashboard">Pulpit</NavLink>
          <NavLink to="/leave-requests">Urlopy</NavLink>
          {hasAnyRole('MANAGER', 'HR_ADMIN') && (
            <NavLink to="/leave-requests/pending">Wnioski do zatwierdzenia</NavLink>
          )}
          <NavLink to="/time-tracking">Czas pracy</NavLink>
        </nav>
        <div className="app-header-user">
          <span>{email}</span>
          <button onClick={logout}>Wyloguj się</button>
        </div>
      </header>
      <main className="app-content">{children}</main>
    </div>
  )
}
