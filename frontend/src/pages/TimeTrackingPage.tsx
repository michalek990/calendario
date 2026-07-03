import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { clockIn, clockOut, listMyTimeEntries } from '../api/timeEntries'
import { ApiError } from '../api/types'
import type { TimeEntry } from '../api/types'

function formatDateTime(value: string | null): string {
  return value ? new Date(value).toLocaleString() : '—'
}

function formatMinutes(value: number | null): string {
  if (value === null) return '—'
  const hours = Math.floor(value / 60)
  const minutes = value % 60
  return `${hours}h ${minutes}min`
}

export function TimeTrackingPage() {
  const { token } = useAuth()
  const [entries, setEntries] = useState<TimeEntry[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadEntries = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setEntries(await listMyTimeEntries(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać wpisów')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadEntries()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const openEntry = entries.find((entry) => entry.clockOut === null) ?? null

  const handleClockIn = async () => {
    if (!token) return
    setError(null)
    setIsSubmitting(true)
    try {
      await clockIn(token)
      await loadEntries()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się rozpocząć pracy')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleClockOut = async () => {
    if (!token) return
    setError(null)
    setIsSubmitting(true)
    try {
      await clockOut(token)
      await loadEntries()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się zakończyć pracy')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Czas pracy</h1>

      {openEntry ? (
        <button disabled={isSubmitting} onClick={handleClockOut}>
          {isSubmitting ? 'Zapisywanie…' : 'Zakończ pracę'}
        </button>
      ) : (
        <button disabled={isSubmitting} onClick={handleClockIn}>
          {isSubmitting ? 'Zapisywanie…' : 'Rozpocznij pracę'}
        </button>
      )}

      {error && <p className="auth-error">{error}</p>}

      {isLoading ? (
        <p>Ładowanie…</p>
      ) : entries.length === 0 ? (
        <p>Brak zarejestrowanych wpisów czasu pracy.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Rozpoczęcie</th>
              <th>Zakończenie</th>
              <th>Przerwa (min)</th>
              <th>Suma</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <tr key={entry.id}>
                <td>{formatDateTime(entry.clockIn)}</td>
                <td>{formatDateTime(entry.clockOut)}</td>
                <td>{entry.breakMinutes}</td>
                <td>{formatMinutes(entry.totalMinutes)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
