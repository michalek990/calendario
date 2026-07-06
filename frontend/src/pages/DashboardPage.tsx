import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { ROLE_LABELS } from '../auth/jwt'
import { listMyLeaveRequests, listRecentLeaveActivity } from '../api/leave'
import { listMyTimeEntries } from '../api/timeEntries'
import { ApiError } from '../api/types'
import type { LeaveRequest } from '../api/types'
import { MonthCalendar, type DayStatus } from '../components/MonthCalendar'
import { eachIsoDateInRange, toIsoDate } from '../utils/calendar'
import { LEAVE_TYPE_LABELS, STATUS_LABELS } from '../constants/labels'

export function DashboardPage() {
  const { token, firstName, lastName, role } = useAuth()
  const [dayStatus, setDayStatus] = useState<Record<string, DayStatus>>({})
  const [recentActivity, setRecentActivity] = useState<LeaveRequest[]>([])
  const [error, setError] = useState<string | null>(null)

  const today = useMemo(() => new Date(), [])
  const year = today.getFullYear()
  const month = today.getMonth()

  useEffect(() => {
    if (!token) return

    async function loadMonthData() {
      try {
        const [leaveRequests, timeEntries] = await Promise.all([
          listMyLeaveRequests(token as string),
          listMyTimeEntries(token as string),
        ])

        const statuses: Record<string, DayStatus> = {}

        for (const entry of timeEntries) {
          const iso = toIsoDate(new Date(entry.clockIn))
          statuses[iso] = 'work'
        }

        for (const request of leaveRequests) {
          if (request.status !== 'APPROVED') continue
          for (const iso of eachIsoDateInRange(request.startDate, request.endDate)) {
            statuses[iso] = 'leave'
          }
        }

        setDayStatus(statuses)
      } catch (err) {
        setError(err instanceof ApiError ? err.message : 'Nie udało się wczytać danych miesiąca')
      }
    }

    async function loadRecentActivity() {
      try {
        setRecentActivity(await listRecentLeaveActivity(token as string))
      } catch {
        // ostatnie zmiany to dodatek — brak nie powinien blokować pulpitu
      }
    }

    loadMonthData()
    loadRecentActivity()
  }, [token])

  const displayName = firstName && lastName ? `${firstName} ${lastName}` : null

  return (
    <div>
      <h1>Witaj{displayName ? `, ${displayName}` : ''}</h1>
      <p>Rola: {role ? (ROLE_LABELS[role] ?? role) : '—'}</p>

      {error && <p className="auth-error">{error}</p>}

      <MonthCalendar year={year} month={month} dayStatus={dayStatus} />

      {recentActivity.length > 0 && (
        <section className="list-section">
          <h2>Ostatnie zmiany na wnioskach</h2>
          <ul className="record-list">
            {recentActivity.map((request) => (
              <li key={request.id} className="record-list-item">
                <div className="record-list-main">
                  <strong>{LEAVE_TYPE_LABELS[request.type]}</strong>
                  <span>
                    {request.startDate} → {request.endDate}
                  </span>
                </div>
                <span className={`status-badge status-${request.status.toLowerCase()}`}>
                  {STATUS_LABELS[request.status]}
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}

      <p>Wybierz moduł z menu powyżej — Urlopy, Czas pracy albo Projekty.</p>
    </div>
  )
}
