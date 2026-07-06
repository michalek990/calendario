import { useEffect, useState, type ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listMyNotifications } from '../api/notifications'
import { UserAvatarMenu } from './UserAvatarMenu'

export function AppLayout({ children }: { children: ReactNode }) {
  const { token, logout, hasAnyRole } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!token) return
    let cancelled = false

    listMyNotifications(token)
      .then((notifications) => {
        if (!cancelled) {
          setUnreadCount(notifications.filter((n) => !n.read).length)
        }
      })
      .catch(() => {
        // brak powiadomień nie powinien blokować reszty layoutu
      })

    return () => {
      cancelled = true
    }
  }, [token])

  return (
    <div className="app-layout">
      <header className="app-header">
        <span className="app-brand">Calendario HR</span>
        <nav>
          <NavLink to="/dashboard">Pulpit</NavLink>
          <NavLink to="/time-tracking">Rejestracja czasu pracy</NavLink>
          <NavLink to="/leave-requests">Wnioski urlopowe</NavLink>
          {hasAnyRole('MANAGER', 'HR', 'ADMIN') && (
            <NavLink to="/leave-requests/pending">Do zatwierdzenia</NavLink>
          )}
          <NavLink to="/projects">Projekty</NavLink>
          <NavLink to="/notifications">
            Powiadomienia{unreadCount > 0 ? ` (${unreadCount})` : ''}
          </NavLink>
          <NavLink to="/profile">Profil</NavLink>
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
