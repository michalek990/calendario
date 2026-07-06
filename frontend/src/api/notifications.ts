import { getJson, patchJson } from './client'
import type { NotificationView } from './types'

export function listMyNotifications(token: string): Promise<NotificationView[]> {
  return getJson<NotificationView[]>('/notifications/me', token)
}

export function markNotificationAsRead(id: number, token: string): Promise<NotificationView> {
  return patchJson<NotificationView>(`/notifications/${id}/read`, undefined, token)
}
