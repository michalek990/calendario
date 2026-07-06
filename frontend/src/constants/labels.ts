import type { LeaveRequest, LeaveType, NotificationType } from '../api/types'

export const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  VACATION: 'Urlop wypoczynkowy',
  ON_DEMAND: 'Urlop na żądanie',
  SICK_LEAVE: 'Zwolnienie lekarskie',
  UNPAID: 'Urlop bezpłatny',
  CHILDCARE_UNPAID: 'Opieka nad dzieckiem (bezpłatna)',
  OCCASIONAL: 'Urlop okolicznościowy',
  REMOTE_WORK: 'Praca z domu',
  HOLIDAY_COMPENSATION: 'Odbiór za święto',
  BUSINESS_TRIP: 'Delegacja',
  OTHER: 'Inny',
}

export const STATUS_LABELS: Record<LeaveRequest['status'], string> = {
  PENDING: 'Oczekuje',
  APPROVED: 'Zatwierdzony',
  REJECTED: 'Odrzucony',
  CANCELLED: 'Anulowany',
}

export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  LEAVE_REQUEST_APPROVED: 'Wniosek zaakceptowany',
  LEAVE_REQUEST_REJECTED: 'Wniosek odrzucony',
}
