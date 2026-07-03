import { getJson, patchJson, postJson } from './client'
import type { CreateLeaveRequestPayload, LeaveRequest } from './types'

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
  return patchJson<LeaveRequest>(`/leave-requests/${id}/approve`, token)
}

export function rejectLeaveRequest(id: number, token: string): Promise<LeaveRequest> {
  return patchJson<LeaveRequest>(`/leave-requests/${id}/reject`, token)
}
