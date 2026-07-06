import { useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { createLeaveRequest, getMyAnnualLeaveSummary, listMyLeaveRequests } from '../api/leave'
import { ApiError } from '../api/types'
import type { AnnualLeaveSummary, LeaveRequest, LeaveType } from '../api/types'
import { LEAVE_TYPE_LABELS, STATUS_LABELS } from '../constants/labels'

export function LeaveRequestsPage() {
  const { token } = useAuth()
  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [summary, setSummary] = useState<AnnualLeaveSummary | null>(null)

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

  const loadSummary = async () => {
    if (!token) return
    try {
      setSummary(await getMyAnnualLeaveSummary(token))
    } catch {
      // brak podsumowania rocznego nie powinien blokować reszty strony
    }
  }

  useEffect(() => {
    loadRequests()
    loadSummary()
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
      await loadSummary()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się złożyć wniosku')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Wnioski urlopowe</h1>

      {summary && (
        <section className="settings-section">
          <h2>Podsumowanie roczne ({summary.year})</h2>
          <div className="profile-grid">
            <div>
              <span className="profile-label">Praca z domu</span>
              <span>{summary.remoteWorkDays} dni</span>
            </div>
            <div>
              <span className="profile-label">Pozostałe nieobecności</span>
              <span>{summary.otherLeaveDays} dni</span>
            </div>
            <div>
              <span className="profile-label">Urlop wypoczynkowy — wykorzystano</span>
              <span>
                {summary.vacationDaysUsed} / {summary.vacationAnnualLimit} dni
              </span>
            </div>
            <div>
              <span className="profile-label">Urlop wypoczynkowy — pozostało</span>
              <span>{summary.vacationDaysRemaining} dni</span>
            </div>
          </div>
        </section>
      )}

      <section className="settings-section">
        <h2>Złóż nowy wniosek</h2>

        <form className="wide-form" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="type">Typ</label>
            <select id="type" value={type} onChange={(e) => setType(e.target.value as LeaveType)}>
              {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="startDate">Od</label>
            <input
              id="startDate"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="endDate">Do</label>
            <input id="endDate" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
          </div>

          <div>
            <label htmlFor="reason">Powód (opcjonalnie)</label>
            <input id="reason" type="text" value={reason} onChange={(e) => setReason(e.target.value)} />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <div className="wide-form-actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Wysyłanie…' : 'Złóż wniosek'}
            </button>
          </div>
        </form>
      </section>

      <section className="list-section">
        <h2>Twoje wnioski</h2>

        {isLoading ? (
          <p>Ładowanie…</p>
        ) : requests.length === 0 ? (
          <p>Nie masz jeszcze żadnych wniosków.</p>
        ) : (
          <ul className="record-list">
            {requests.map((request) => (
              <li key={request.id} className="record-list-item">
                <div className="record-list-main">
                  <strong>{LEAVE_TYPE_LABELS[request.type]}</strong>
                  <span>
                    {request.startDate} → {request.endDate} ({request.daysCount} dni)
                  </span>
                  {request.reason && <span className="record-list-reason">{request.reason}</span>}
                </div>
                <span className={`status-badge status-${request.status.toLowerCase()}`}>
                  {STATUS_LABELS[request.status]}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
