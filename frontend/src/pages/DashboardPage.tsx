import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { ROLE_LABELS } from '../auth/jwt'
import { getMyAnnualLeaveSummary, listMyLeaveRequests, listRecentLeaveActivity } from '../api/leave'
import { clockIn, clockOut, listMyTimeEntries } from '../api/timeEntries'
import { listProjects } from '../api/projects'
import { listMyNotifications } from '../api/notifications'
import { ApiError } from '../api/types'
import type { LeaveRequest, Project, TimeEntry } from '../api/types'
import { MonthCalendar, type DayStatus } from '../components/MonthCalendar'
import { eachIsoDateInRange, toIsoDate } from '../utils/calendar'
import { LEAVE_TYPE_LABELS, STATUS_LABELS } from '../constants/labels'
import { IconBell, IconCalendar, IconClock } from '../components/icons'

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatDuration(totalMinutes: number): string {
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  return `${hours}h ${minutes}min`
}

function currentTimeInputValue(): string {
  const now = new Date()
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

function combineDateAndTime(date: Date, timeValue: string): string {
  const isoDate = toIsoDate(date)
  return new Date(`${isoDate}T${timeValue}:00`).toISOString()
}

export function DashboardPage() {
  const { token, firstName, lastName, role } = useAuth()
  const [dayStatus, setDayStatus] = useState<Record<string, DayStatus>>({})
  const [recentActivity, setRecentActivity] = useState<LeaveRequest[]>([])
  const [vacationDaysRemaining, setVacationDaysRemaining] = useState<number | null>(null)
  const [hoursThisMonth, setHoursThisMonth] = useState<number | null>(null)
  const [unreadCount, setUnreadCount] = useState(0)
  const [activeEntry, setActiveEntry] = useState<TimeEntry | null>(null)
  const [todayFinishedEntries, setTodayFinishedEntries] = useState<TimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState('')
  const [startTimeInput, setStartTimeInput] = useState(currentTimeInputValue)
  const [endTimeInput, setEndTimeInput] = useState(currentTimeInputValue)
  const [isClockLoading, setIsClockLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [clockError, setClockError] = useState<string | null>(null)

  const today = useMemo(() => new Date(), [])
  const year = today.getFullYear()
  const month = today.getMonth()

  const loadMonthData = async (currentToken: string) => {
    try {
      const [leaveRequests, timeEntries] = await Promise.all([
        listMyLeaveRequests(currentToken),
        listMyTimeEntries(currentToken),
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
      setActiveEntry(timeEntries.find((entry) => entry.clockOut === null) ?? null)

      const todayIso = toIsoDate(today)
      setTodayFinishedEntries(
        timeEntries
          .filter((entry) => entry.clockOut !== null && toIsoDate(new Date(entry.clockIn)) === todayIso)
          .sort((a, b) => new Date(a.clockOut as string).getTime() - new Date(b.clockOut as string).getTime()),
      )

      const totalMinutesThisMonth = timeEntries
        .filter((entry) => {
          const clockInDate = new Date(entry.clockIn)
          return clockInDate.getFullYear() === year && clockInDate.getMonth() === month
        })
        .reduce((sum, entry) => sum + (entry.totalMinutes ?? 0), 0)
      setHoursThisMonth(Math.round((totalMinutesThisMonth / 60) * 10) / 10)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się wczytać danych miesiąca')
    }
  }

  useEffect(() => {
    if (!token) return

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

    async function loadProjects() {
      try {
        setProjects(await listProjects(token as string))
      } catch {
        // brak listy projektów nie blokuje rozpoczęcia pracy bez projektu
      }
    }

    loadMonthData(token)
    loadRecentActivity()
    loadAnnualSummary()
    loadNotifications()
    loadProjects()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const resetClockInputs = () => {
    setStartTimeInput(currentTimeInputValue())
    setEndTimeInput(currentTimeInputValue())
  }

  const handleClockIn = async () => {
    if (!token) return
    setClockError(null)
    setIsClockLoading(true)
    try {
      await clockIn(
        token,
        selectedProjectId ? Number(selectedProjectId) : undefined,
        combineDateAndTime(today, startTimeInput),
      )
      await loadMonthData(token)
      resetClockInputs()
    } catch (err) {
      setClockError(err instanceof ApiError ? err.message : 'Nie udało się rozpocząć pracy')
    } finally {
      setIsClockLoading(false)
    }
  }

  const handleClockOut = async () => {
    if (!token) return
    setClockError(null)
    setIsClockLoading(true)
    try {
      await clockOut(token, combineDateAndTime(today, endTimeInput))
      await loadMonthData(token)
      resetClockInputs()
    } catch (err) {
      setClockError(err instanceof ApiError ? err.message : 'Nie udało się zakończyć pracy')
    } finally {
      setIsClockLoading(false)
    }
  }

  const displayName = firstName && lastName ? `${firstName} ${lastName}` : null
  const activeProjectName = activeEntry?.projectId
    ? projects.find((project) => project.id === activeEntry.projectId)?.name
    : null
  const totalMinutesToday = todayFinishedEntries.reduce((sum, entry) => sum + (entry.totalMinutes ?? 0), 0)
  const lastFinishedToday = todayFinishedEntries.length > 0 ? todayFinishedEntries[todayFinishedEntries.length - 1] : null

  return (
    <div>
      <div className="page-header">
        <h1>Witaj{displayName ? `, ${displayName}` : ''}</h1>
        <p className="page-subtitle">Rola: {role ? (ROLE_LABELS[role] ?? role) : '—'}</p>
      </div>

      {error && <p className="auth-error">{error}</p>}

      <div className="clock-widget">
        <div className="clock-widget-icon">
          <IconClock />
        </div>

        <div className="clock-widget-body">
          {activeEntry ? (
            <>
              <span className="clock-widget-status">Pracujesz od {formatTime(activeEntry.clockIn)}</span>
              {activeProjectName && <span className="clock-widget-hint">Projekt: {activeProjectName}</span>}
              <label className="clock-widget-time-label" htmlFor="endTimeInput">
                Godzina zakończenia
                <input
                  id="endTimeInput"
                  type="time"
                  className="clock-widget-select"
                  value={endTimeInput}
                  onChange={(e) => setEndTimeInput(e.target.value)}
                  disabled={isClockLoading}
                />
              </label>
            </>
          ) : (
            <>
              <span className="clock-widget-status">
                {lastFinishedToday
                  ? `Dziś ${formatTime(lastFinishedToday.clockIn)}–${formatTime(lastFinishedToday.clockOut as string)} · przepracowano ${formatDuration(totalMinutesToday)}`
                  : 'Nie rozpoczęto dzisiaj pracy'}
              </span>
              <label className="clock-widget-time-label" htmlFor="startTimeInput">
                Godzina rozpoczęcia
                <input
                  id="startTimeInput"
                  type="time"
                  className="clock-widget-select"
                  value={startTimeInput}
                  onChange={(e) => setStartTimeInput(e.target.value)}
                  disabled={isClockLoading}
                />
              </label>
              {projects.length > 0 && (
                <select
                  className="clock-widget-select"
                  value={selectedProjectId}
                  onChange={(e) => setSelectedProjectId(e.target.value)}
                  disabled={isClockLoading}
                  aria-label="Projekt"
                >
                  <option value="">Brak projektu</option>
                  {projects.map((project) => (
                    <option key={project.id} value={project.id}>
                      {project.name}
                    </option>
                  ))}
                </select>
              )}
            </>
          )}
          {clockError && <p className="auth-error">{clockError}</p>}
        </div>

        {activeEntry ? (
          <button
            type="button"
            className="clock-widget-button clock-widget-button-stop"
            onClick={handleClockOut}
            disabled={isClockLoading}
          >
            {isClockLoading ? 'Zapisywanie…' : 'Zakończ'}
          </button>
        ) : (
          <button type="button" className="clock-widget-button" onClick={handleClockIn} disabled={isClockLoading}>
            {isClockLoading ? 'Zapisywanie…' : 'Rozpocznij'}
          </button>
        )}
      </div>

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
          <span className="stat-card-label">Godziny w tym miesiącu</span>
          <span className="stat-card-value">{hoursThisMonth ?? '—'}</span>
          <span className="stat-card-hint">zarejestrowanych godzin pracy</span>
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
