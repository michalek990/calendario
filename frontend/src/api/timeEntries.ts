import { getJson, postJson } from './client'
import type { TimeEntry } from './types'

export function clockIn(token: string): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/clock-in', undefined, token)
}

export function clockOut(token: string): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/clock-out', undefined, token)
}

export function listMyTimeEntries(token: string): Promise<TimeEntry[]> {
  return getJson<TimeEntry[]>('/time-entries/me', token)
}
