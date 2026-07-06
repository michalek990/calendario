import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { ROLE_LABELS } from '../auth/jwt'
import { getMyAnnualLeaveSummary, listMyLeaveRequests, listRecentLeaveActivity } from '../api/leave'
import { listMyTimeEntries } from '../api/timeEntries'
import { listMyNotifications } from '../api/notifications'
import { ApiError } from '../api/types'
import type { LeaveRequest } from '../api/types'
import { MonthCalendar, type DayStatus } from '../components/MonthCalendar'
import { eachIsoDateInRange, toIsoDate } from '../utils/calendar'
import { LEAVE_TYPE_LABELS, STATUS_LABELS } from '../constants/labels'
import { IconBell, IconCalendar, IconClock } from '../components/icons'

export function DashboardPage() {
  const { token, firstName, lastName, role } = useAuth()
  const [dayStatus, setDayStatus] = useState<Record<string, DayStatus>>({})
  const [recentActivity, setRecentActivity] = useState<LeaveRequest[]>([])
  const [vacationDaysRemaining, setVacationDaysRemaining] = useState<number | null>(null)
  const [isWorking, setIsWorking] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
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
        setIsWorking(timeEntries.some((entry) => entry.clockOut === null))
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

    async function loadAnnualSummary() {
      try {
        const summary = await getMyAnnualLeaveSummary(token as string)
        setVacationDaysRemaining(summary.vacationDaysRemaining)
      } catch {
        // widget urlopu to dodatek — brak nie powinien blokować pulpitu
      }
    }

    async function loadNotifications() {
      try {
        const notifications = await listMyNotifications(token as string)
        setUnreadCount(notifications.filter((n) => !n.read).length)
      } catch {
        // licznik powiadomień to dodatek — brak nie powinien blokować pulpitu
      }
    }

    loadMonthData()
    loadRecentActivity()
    loadAnnualSummary()
    loadNotifications()
  }, [token])

  const displayName = firstName && lastName ? `${firstName} ${lastName}` : null

  return (
    <div>
      <div className="page-header">
        <h1>Witaj{displayName ? `, ${displayName}` : ''}</h1>
        <p className="page-subtitle">Rola: {role ? (ROLE_LABELS[role] ?? role) : '—'}</p>
      </div>

      {error && <p className="auth-error">{error}</p>}

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-card-icon">
            <IconCalendar />
          </div>
          <span className="stat-card-label">Pozostały urlop</span>
          <span className="stat-card-value">{vacationDaysRemaining ?? '—'}</span>
          <span className="stat-card-hint">dni wypoczynkowego</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon">
            <IconClock />
          </div>
          <span className="stat-card-label">Status pracy</span>
          <span className="stat-card-value">{isWorking ? 'W pracy' : 'Poza pracą'}</span>
          <span className="stat-card-hint">{isWorking ? 'Masz otwarty wpis czasu pracy' : 'Brak otwartego wpisu'}</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon">
            <IconBell />
          </div>
          <span className="stat-card-label">Powiadomienia</span>
          <span className="stat-card-value">{unreadCount}</span>
          <span className="stat-card-hint">nieprzeczytanych</span>
        </div>
      </div>

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
    </div>
  )
}
