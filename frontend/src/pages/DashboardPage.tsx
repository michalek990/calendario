import { useAuth } from '../auth/AuthContext'

export function DashboardPage() {
  const { email, role } = useAuth()

  return (
    <div>
      <h1>Witaj, {email}</h1>
      <p>Rola: {role}</p>
      <p>Wybierz moduł z menu powyżej — Urlopy albo Czas pracy.</p>
    </div>
  )
}
