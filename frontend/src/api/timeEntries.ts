import { getJson, postJson } from './client'
import type { ProjectTimeSummary, TimeEntry } from './types'

export function clockIn(token: string, projectId?: number | null): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/clock-in', projectId ? { projectId } : undefined, token)
}

export function clockOut(token: string): Promise<TimeEntry> {
  return postJson<TimeEntry>('/time-entries/clock-out', undefined, token)
}

export function listMyTimeEntries(token: string): Promise<TimeEntry[]> {
  return getJson<TimeEntry[]>('/time-entries/me', token)
}

export function listMyTimeByProject(token: string): Promise<ProjectTimeSummary[]> {
  return getJson<ProjectTimeSummary[]>('/time-entries/me/by-project', token)
}
