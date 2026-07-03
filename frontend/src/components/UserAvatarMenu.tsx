import { useEffect, useRef, useState } from 'react'
import { useAuth } from '../auth/AuthContext'

const ROLE_LABELS: Record<string, string> = {
  EMPLOYEE: 'Pracownik',
  MANAGER: 'Kierownik',
  HR_ADMIN: 'Administrator HR',
}

export function UserAvatarMenu() {
  const { firstName, lastName, email, role } = useAuth()
  const [isOpen, setIsOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const initials = firstName && lastName ? `${firstName[0]}${lastName[0]}`.toUpperCase() : '?'
  const displayName = firstName && lastName ? `${firstName} ${lastName}` : email

  return (
    <div className="avatar-menu" ref={containerRef}>
      <button
        type="button"
        className="avatar-circle"
        onClick={() => setIsOpen((open) => !open)}
        aria-label="Profil użytkownika"
      >
        {initials}
      </button>

      {isOpen && (
        <div className="avatar-popup">
          <p className="avatar-popup-name">{displayName}</p>
          <p className="avatar-popup-email">{email}</p>
          <p className="avatar-popup-role">{role ? (ROLE_LABELS[role] ?? role) : '—'}</p>
        </div>
      )}
    </div>
  )
}
