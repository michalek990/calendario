import { useAuth } from '../auth/AuthContext'

export function DashboardPage() {
  const { email, logout } = useAuth()

  return (
    <div className="dashboard-page">
      <h1>Witaj, {email}</h1>
      <p>Jesteś zalogowany do systemu Calendario HR.</p>
      <button onClick={logout}>Wyloguj się</button>
    </div>
  )
}
