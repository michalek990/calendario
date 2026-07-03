export interface CalendarDay {
  date: Date
  iso: string
  inCurrentMonth: boolean
}

export function toIsoDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/** Poniedziałek = 0 ... niedziela = 6 (w przeciwieństwie do Date.getDay(), gdzie niedziela = 0). */
function mondayFirstWeekday(date: Date): number {
  return (date.getDay() + 6) % 7
}

/** Siatka miesiąca w tygodniach (po 7 dni), z dniami spoza miesiąca jako wypełnienie. */
export function getMonthGrid(year: number, month: number): CalendarDay[][] {
  const firstOfMonth = new Date(year, month, 1)
  const leadingBlanks = mondayFirstWeekday(firstOfMonth)
  const daysInMonth = new Date(year, month + 1, 0).getDate()

  const cells: CalendarDay[] = []

  for (let i = 0; i < leadingBlanks; i++) {
    cells.push({ date: new Date(year, month, 0), iso: '', inCurrentMonth: false })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    cells.push({ date, iso: toIsoDate(date), inCurrentMonth: true })
  }

  while (cells.length % 7 !== 0) {
    cells.push({ date: new Date(year, month + 1, 1), iso: '', inCurrentMonth: false })
  }

  const weeks: CalendarDay[][] = []
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7))
  }
  return weeks
}

/** Zwraca listę dat ISO (YYYY-MM-DD) z zakresu [startIso, endIso], włącznie. */
export function eachIsoDateInRange(startIso: string, endIso: string): string[] {
  const start = new Date(`${startIso}T00:00:00`)
  const end = new Date(`${endIso}T00:00:00`)
  const dates: string[] = []

  for (let current = start; current <= end; current = new Date(current.getFullYear(), current.getMonth(), current.getDate() + 1)) {
    dates.push(toIsoDate(current))
  }

  return dates
}
