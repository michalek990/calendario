import type { Role } from '../auth/jwt'

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

export interface ChangePasswordPayload {
  currentPassword: string
  newPassword: string
}

export interface AuthResponse {
  token: string
}

export interface ApiErrorBody {
  message: string
}

export type LeaveType =
  | 'VACATION'
  | 'ON_DEMAND'
  | 'SICK_LEAVE'
  | 'UNPAID'
  | 'CHILDCARE_UNPAID'
  | 'OCCASIONAL'
  | 'REMOTE_WORK'
  | 'HOLIDAY_COMPENSATION'
  | 'BUSINESS_TRIP'
  | 'OTHER'

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

export interface AnnualLeaveSummary {
  year: number
  daysByType: Partial<Record<LeaveType, number>>
  remoteWorkDays: number
  otherLeaveDays: number
  vacationDaysUsed: number
  vacationDaysRemaining: number
  vacationAnnualLimit: number
}

export interface TimeEntry {
  id: number
  userId: number
  clockIn: string
  clockOut: string | null
  breakMinutes: number
  totalMinutes: number | null
  notes: string | null
  projectId: number | null
}

export interface UserProfile {
  id: number
  email: string
  firstName: string
  lastName: string
  role: Role
  position: string | null
  department: string | null
  facility: string | null
  isSupervisor: boolean
  hasSupervisor: boolean
  supervisorId: number | null
  supervisorFullName: string | null
  birthDate: string | null
  phoneNumber: string | null
  avatarUrl: string | null
  lastLoginAt: string | null
}

export interface UpdatePersonalInfoPayload {
  birthDate?: string | null
  phoneNumber?: string | null
  avatarUrl?: string | null
}

export interface UpdateOrganizationPayload {
  position?: string | null
  department?: string | null
  facility?: string | null
  supervisorId?: number | null
}

export type NotificationType = 'LEAVE_REQUEST_APPROVED' | 'LEAVE_REQUEST_REJECTED'

export interface NotificationView {
  id: number
  type: NotificationType
  message: string
  leaveRequestId: number | null
  read: boolean
  createdAt: string
}

export interface Project {
  id: number
  name: string
  description: string | null
  createdAt: string
}

export interface CreateProjectPayload {
  name: string
  description?: string
}

export interface ProjectTimeSummary {
  projectId: number
  projectName: string
  totalMinutes: number
  entryCount: number
}

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}
