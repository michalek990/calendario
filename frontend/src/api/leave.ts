import { getJson, patchJson, postJson } from './client'
import type { AnnualLeaveSummary, CreateLeaveRequestPayload, LeaveRequest } from './types'

export function createLeaveRequest(payload: CreateLeaveRequestPayload, token: string): Promise<LeaveRequest> {
  return postJson<LeaveRequest>('/leave-requests', payload, token)
}

export function listMyLeaveRequests(token: string): Promise<LeaveRequest[]> {
  return getJson<LeaveRequest[]>('/leave-requests/me', token)
}

export function listPendingLeaveRequests(token: string): Promise<LeaveRequest[]> {
  return getJson<LeaveRequest[]>('/leave-requests/pending', token)
}

export function approveLeaveRequest(id: number, token: string): Promise<LeaveRequest> {
  return patchJson<LeaveRequest>(`/leave-requests/${id}/approve`, undefined, token)
}

export function rejectLeaveRequest(id: number, token: string): Promise<LeaveRequest> {
  return patchJson<LeaveRequest>(`/leave-requests/${id}/reject`, undefined, token)
}

export function listRecentLeaveActivity(token: string): Promise<LeaveRequest[]> {
  return getJson<LeaveRequest[]>('/leave-requests/me/recent-activity', token)
}

export function getMyAnnualLeaveSummary(token: string, year?: number): Promise<AnnualLeaveSummary> {
  const query = year ? `?year=${year}` : ''
  return getJson<AnnualLeaveSummary>(`/leave-requests/me/annual-summary${query}`, token)
}
