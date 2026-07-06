import { getJson, postJson, putJson } from './client'
import type { LogTimeEntryPayload, ManagedTimeEntry, ProjectTimeSummary, TimeEntry, UpdateTimeEntryPayload } from './types'

export function clockIn(token: string, projectId?: number | null, clockInAt?: string): Promise<TimeEntry> {
  const body = projectId || clockInAt ? { projectId: projectId ?? undefined, clockIn: clockInAt } : undefined
  return postJson<TimeEntry>('/time-entries/clock-in', body, token)
}

export function logTimeEntry(payload: LogTimeEntryPayload, token: string): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/log', payload, token)
}

export function clockOut(token: string, clockOutAt?: string): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/clock-out', clockOutAt ? { clockOut: clockOutAt } : undefined, token)
}

export function updateTimeEntry(id: number, payload: UpdateTimeEntryPayload, token: string): Promise<TimeEntry> {
  return putJson<TimeEntry>(`/time-entries/${id}`, payload, token)
}

export function listMyTimeEntries(token: string): Promise<TimeEntry[]> {
  return getJson<TimeEntry[]>('/time-entries/me', token)
}

export function listMyTimeByProject(token: string): Promise<ProjectTimeSummary[]> {
  return getJson<ProjectTimeSummary[]>('/time-entries/me/by-project', token)
}

export function listManagedTimeEntries(token: string): Promise<ManagedTimeEntry[]> {
  return getJson<ManagedTimeEntry[]>('/time-entries', token)
}
