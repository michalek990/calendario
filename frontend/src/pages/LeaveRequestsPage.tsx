import { useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { createLeaveRequest, listMyLeaveRequests } from '../api/leave'
import { ApiError } from '../api/types'
import type { LeaveRequest, LeaveType } from '../api/types'

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  VACATION: 'Urlop wypoczynkowy',
  SICK_LEAVE: 'Zwolnienie lekarskie',
  UNPAID: 'Urlop bezpłatny',
  OTHER: 'Inny',
}

const STATUS_LABELS: Record<LeaveRequest['status'], string> = {
  PENDING: 'Oczekuje',
  APPROVED: 'Zatwierdzony',
  REJECTED: 'Odrzucony',
  CANCELLED: 'Anulowany',
}

export function LeaveRequestsPage() {
  const { token } = useAuth()
  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [type, setType] = useState<LeaveType>('VACATION')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [reason, setReason] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadRequests = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setRequests(await listMyLeaveRequests(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać wniosków')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadRequests()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!token) return
    setError(null)
    setIsSubmitting(true)
    try {
      await createLeaveRequest({ type, startDate, endDate, reason: reason || undefined }, token)
      setStartDate('')
      setEndDate('')
      setReason('')
      await loadRequests()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się złożyć wniosku')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Wnioski urlopowe</h1>

      <form className="inline-form" onSubmit={handleSubmit}>
        <label htmlFor="type">Typ</label>
        <select id="type" value={type} onChange={(e) => setType(e.target.value as LeaveType)}>
          {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>

        <label htmlFor="startDate">Od</label>
        <input
          id="startDate"
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          required
        />

        <label htmlFor="endDate">Do</label>
        <input id="endDate" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />

        <label htmlFor="reason">Powód (opcjonalnie)</label>
        <input id="reason" type="text" value={reason} onChange={(e) => setReason(e.target.value)} />

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Wysyłanie…' : 'Złóż wniosek'}
        </button>
      </form>

      {error && <p className="auth-error">{error}</p>}

      {isLoading ? (
        <p>Ładowanie…</p>
      ) : requests.length === 0 ? (
        <p>Nie masz jeszcze żadnych wniosków.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Typ</th>
              <th>Od</th>
              <th>Do</th>
              <th>Dni</th>
              <th>Status</th>
              <th>Powód</th>
            </tr>
          </thead>
          <tbody>
            {requests.map((request) => (
              <tr key={request.id}>
                <td>{LEAVE_TYPE_LABELS[request.type]}</td>
                <td>{request.startDate}</td>
                <td>{request.endDate}</td>
                <td>{request.daysCount}</td>
                <td>
                  <span className={`status-badge status-${request.status.toLowerCase()}`}>
                    {STATUS_LABELS[request.status]}
                  </span>
                </td>
                <td>{request.reason ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
