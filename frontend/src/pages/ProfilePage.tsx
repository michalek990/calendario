import { useAuth } from '../auth/AuthContext'

const ROLE_LABELS: Record<string, string> = {
  EMPLOYEE: 'Pracownik',
  MANAGER: 'Kierownik',
  HR_ADMIN: 'Administrator HR',
}

export function ProfilePage() {
  const { firstName, lastName, email, role } = useAuth()

  return (
    <div>
      <h1>Profil użytkownika</h1>

      <table>
        <tbody>
          <tr>
            <th>Imię i nazwisko</th>
            <td>{firstName && lastName ? `${firstName} ${lastName}` : '—'}</td>
          </tr>
          <tr>
            <th>E-mail</th>
            <td>{email}</td>
          </tr>
          <tr>
            <th>Rola</th>
            <td>{role ? (ROLE_LABELS[role] ?? role) : '—'}</td>
          </tr>
        </tbody>
      </table>
    </div>
  )
}
