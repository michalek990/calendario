import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { listMyNotifications, markNotificationAsRead } from '../api/notifications'
import { ApiError } from '../api/types'
import type { NotificationView } from '../api/types'
import { NOTIFICATION_TYPE_LABELS } from '../constants/labels'

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString()
}

export function NotificationsPage() {
  const { token } = useAuth()
  const [notifications, setNotifications] = useState<NotificationView[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const loadNotifications = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setNotifications(await listMyNotifications(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać powiadomień')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadNotifications()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleMarkAsRead = async (id: number) => {
    if (!token) return
    setProcessingId(id)
    try {
      await markNotificationAsRead(id, token)
      await loadNotifications()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się oznaczyć jako przeczytane')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div>
      <h1>Powiadomienia</h1>

      {error && <p className="auth-error">{error}</p>}

      <section className="list-section">
        {isLoading ? (
          <p>Ładowanie…</p>
        ) : notifications.length === 0 ? (
          <p>Brak powiadomień.</p>
        ) : (
          <ul className="record-list">
            {notifications.map((notification) => (
              <li
                key={notification.id}
                className={`record-list-item ${notification.read ? '' : 'record-list-item-unread'}`}
              >
                <div className="record-list-main">
                  <strong>{NOTIFICATION_TYPE_LABELS[notification.type]}</strong>
                  <span>{notification.message}</span>
                  <span className="record-list-reason">{formatDateTime(notification.createdAt)}</span>
                </div>
                {notification.read ? (
                  <span className="status-badge status-cancelled">Przeczytane</span>
                ) : (
                  <button
                    className="btn-secondary"
                    disabled={processingId === notification.id}
                    onClick={() => handleMarkAsRead(notification.id)}
                  >
                    {processingId === notification.id ? 'Zapisywanie…' : 'Oznacz jako przeczytane'}
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
