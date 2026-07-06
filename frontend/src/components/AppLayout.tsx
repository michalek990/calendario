import { useEffect, useState, type ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listMyNotifications } from '../api/notifications'
import { UserAvatarMenu } from './UserAvatarMenu'
import {
  IconBell,
  IconCalendar,
  IconCheckCircle,
  IconClock,
  IconFolder,
  IconHome,
  IconLogOut,
  IconSettings,
  IconUser,
} from './icons'

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
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-mark">C</span>
          <span className="brand-name">Calendario HR</span>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard" className="sidebar-link">
            <IconHome /> <span>Pulpit</span>
          </NavLink>
          <NavLink to="/time-tracking" className="sidebar-link">
            <IconClock /> <span>Czas pracy</span>
          </NavLink>
          <NavLink to="/leave-requests" end className="sidebar-link">
            <IconCalendar /> <span>Wnioski urlopowe</span>
          </NavLink>
          {hasAnyRole('MANAGER', 'HR', 'ADMIN') && (
            <NavLink to="/leave-requests/pending" className="sidebar-link">
              <IconCheckCircle /> <span>Do zatwierdzenia</span>
            </NavLink>
          )}
          <NavLink to="/projects" className="sidebar-link">
            <IconFolder /> <span>Projekty</span>
          </NavLink>
          <NavLink to="/notifications" className="sidebar-link">
            <IconBell /> <span>Powiadomienia</span>
            {unreadCount > 0 && <span className="sidebar-badge">{unreadCount}</span>}
          </NavLink>
          <NavLink to="/profile" className="sidebar-link">
            <IconUser /> <span>Profil</span>
          </NavLink>
          <NavLink to="/settings" className="sidebar-link">
            <IconSettings /> <span>Ustawienia</span>
          </NavLink>
        </nav>

        <button className="sidebar-logout" onClick={logout}>
          <IconLogOut /> <span>Wyloguj się</span>
        </button>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div className="topbar-spacer" />
          <div className="topbar-actions">
            <NavLink to="/notifications" className="icon-button" aria-label="Powiadomienia">
              <IconBell />
              {unreadCount > 0 && <span className="icon-button-badge">{unreadCount}</span>}
            </NavLink>
            <UserAvatarMenu />
          </div>
        </header>
        <main className="app-content">{children}</main>
      </div>
    </div>
  )
}
