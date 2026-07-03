import { getMonthGrid, toIsoDate } from '../utils/calendar'

export type DayStatus = 'leave' | 'work'

interface MonthCalendarProps {
  year: number
  month: number
  dayStatus: Record<string, DayStatus>
}

const WEEKDAY_LABELS = ['Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob', 'Nd']

const MONTH_LABELS = [
  'styczeń',
  'luty',
  'marzec',
  'kwiecień',
  'maj',
  'czerwiec',
  'lipiec',
  'sierpień',
  'wrzesień',
  'październik',
  'listopad',
  'grudzień',
]

export function MonthCalendar({ year, month, dayStatus }: MonthCalendarProps) {
  const weeks = getMonthGrid(year, month)
  const todayIso = toIsoDate(new Date())

  return (
    <div className="month-calendar">
      <div className="month-calendar-header">
        <h2>
          {MONTH_LABELS[month]} {year}
        </h2>
      </div>

      <div className="month-calendar-legend">
        <span>
          <span className="legend-dot leave" /> Urlop zatwierdzony
        </span>
        <span>
          <span className="legend-dot work" /> Zarejestrowany czas pracy
        </span>
      </div>

      <div className="month-calendar-grid">
        {WEEKDAY_LABELS.map((label) => (
          <div key={label} className="month-calendar-weekday">
            {label}
          </div>
        ))}

        {weeks.flatMap((week, weekIndex) =>
          week.map((day, dayIndex) => {
            const status = day.inCurrentMonth ? dayStatus[day.iso] : undefined
            const classNames = ['month-calendar-day']
            if (!day.inCurrentMonth) classNames.push('outside')
            if (status) classNames.push(status)
            if (day.inCurrentMonth && day.iso === todayIso) classNames.push('today')

            return (
              <div key={`${weekIndex}-${dayIndex}`} className={classNames.join(' ')}>
                {day.inCurrentMonth ? day.date.getDate() : ''}
              </div>
            )
          }),
        )}
      </div>
    </div>
  )
}
