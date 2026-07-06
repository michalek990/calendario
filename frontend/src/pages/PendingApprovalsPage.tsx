import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { approveLeaveRequest, listPendingLeaveRequests, rejectLeaveRequest } from '../api/leave'
import { ApiError } from '../api/types'
import type { LeaveRequest } from '../api/types'
import { LEAVE_TYPE_LABELS } from '../constants/labels'

export function PendingApprovalsPage() {
  const { token } = useAuth()
  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const loadRequests = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setRequests(await listPendingLeaveRequests(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać wniosków')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadRequests()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleDecision = async (id: number, decide: (id: number, token: string) => Promise<LeaveRequest>) => {
    if (!token) return
    setError(null)
    setProcessingId(id)
    try {
      await decide(id, token)
      await loadRequests()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się zapisać decyzji')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div>
      <h1>Wnioski do zatwierdzenia</h1>

      {error && <p className="auth-error">{error}</p>}

      {isLoading ? (
        <p>Ładowanie…</p>
      ) : requests.length === 0 ? (
        <p>Brak wniosków oczekujących na decyzję.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Pracownik</th>
              <th>Typ</th>
              <th>Od</th>
              <th>Do</th>
              <th>Dni</th>
              <th>Powód</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {requests.map((request) => (
              <tr key={request.id}>
                <td>#{request.requesterId}</td>
                <td>{LEAVE_TYPE_LABELS[request.type]}</td>
                <td>{request.startDate}</td>
                <td>{request.endDate}</td>
                <td>{request.daysCount}</td>
                <td>{request.reason ?? '—'}</td>
                <td className="actions">
                  <button
                    disabled={processingId === request.id}
                    onClick={() => handleDecision(request.id, approveLeaveRequest)}
                  >
                    Zatwierdź
                  </button>
                  <button
                    disabled={processingId === request.id}
                    onClick={() => handleDecision(request.id, rejectLeaveRequest)}
                  >
                    Odrzuć
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
