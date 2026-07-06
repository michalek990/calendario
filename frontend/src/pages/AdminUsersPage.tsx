import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listAllUsers } from '../api/users'
import { ApiError } from '../api/types'
import type { UserProfile } from '../api/types'
import { ROLE_LABELS } from '../auth/jwt'

function initialsOf(user: UserProfile): string {
  return `${user.firstName[0] ?? ''}${user.lastName[0] ?? ''}`.toUpperCase()
}

export function AdminUsersPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const [users, setUsers] = useState<UserProfile[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    setIsLoading(true)
    listAllUsers(token)
      .then(setUsers)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać listy użytkowników'))
      .finally(() => setIsLoading(false))
  }, [token])

  return (
    <div>
      <div className="page-header">
        <h1>Administracja użytkownikami</h1>
        <p className="page-subtitle">Role, przynależność organizacyjna i przełożeni — dla całej firmy.</p>
      </div>

      {error && <p className="auth-error">{error}</p>}

      {isLoading ? (
        <p>Ładowanie…</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Pracownik</th>
              <th>Rola</th>
              <th>Stanowisko</th>
              <th>Dział</th>
              <th>Zakład</th>
              <th>Przełożony</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>
                  <div className="person-cell">
                    <span className="person-avatar">{initialsOf(user)}</span>
                    <div>
                      <div className="person-name">
                        {user.firstName} {user.lastName}
                      </div>
                      <div className="person-email">{user.email}</div>
                    </div>
                  </div>
                </td>
                <td>
                  <span className={`role-badge role-badge-${user.role}`}>
                    {ROLE_LABELS[user.role] ?? user.role}
                  </span>
                </td>
                <td>{user.position ?? '—'}</td>
                <td>{user.department ?? '—'}</td>
                <td>{user.facility ?? '—'}</td>
                <td>{user.hasSupervisor ? user.supervisorFullName : '—'}</td>
                <td className="actions">
                  <button onClick={() => navigate(`/admin/users/${user.id}`)}>Edytuj</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
