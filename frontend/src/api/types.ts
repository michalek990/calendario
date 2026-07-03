export interface RegisterPayload {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
}

export interface ApiErrorBody {
  message: string
}

export type LeaveType = 'VACATION' | 'SICK_LEAVE' | 'UNPAID' | 'OTHER'

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface LeaveRequest {
  id: number
  requesterId: number
  type: LeaveType
  startDate: string
  endDate: string
  daysCount: number
  status: LeaveStatus
  reason: string | null
  approverId: number | null
  approvedAt: string | null
  createdAt: string
}

export interface CreateLeaveRequestPayload {
  type: LeaveType
  startDate: string
  endDate: string
  reason?: string
}

export interface TimeEntry {
  id: number
  userId: number
  clockIn: string
  clockOut: string | null
  breakMinutes: number
  totalMinutes: number | null
  notes: string | null
}

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}
