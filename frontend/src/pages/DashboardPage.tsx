import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { listMyLeaveRequests } from '../api/leave'
import { listMyTimeEntries } from '../api/timeEntries'
import { ApiError } from '../api/types'
import { MonthCalendar, type DayStatus } from '../components/MonthCalendar'
import { eachIsoDateInRange, toIsoDate } from '../utils/calendar'

const ROLE_LABELS: Record<string, string> = {
  EMPLOYEE: 'Pracownik',
  MANAGER: 'Kierownik',
  HR_ADMIN: 'Administrator HR',
}

export function DashboardPage() {
  const { token, firstName, lastName, role } = useAuth()
  const [dayStatus, setDayStatus] = useState<Record<string, DayStatus>>({})
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

    loadMonthData()
  }, [token])

  const displayName = firstName && lastName ? `${firstName} ${lastName}` : null

  return (
    <div>
      <h1>Witaj{displayName ? `, ${displayName}` : ''}</h1>
      <p>Rola: {role ? (ROLE_LABELS[role] ?? role) : '—'}</p>

      {error && <p className="auth-error">{error}</p>}

      <MonthCalendar year={year} month={month} dayStatus={dayStatus} />

      <p>Wybierz moduł z menu powyżej — Urlopy albo Czas pracy.</p>
    </div>
  )
}
