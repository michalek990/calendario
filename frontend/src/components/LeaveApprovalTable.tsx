import type { LeaveRequest } from '../api/types'
import { LEAVE_TYPE_LABELS } from '../constants/labels'

export function LeaveApprovalTable({
  requests,
  isLoading,
  error,
  processingId,
  requesterNameById,
  onApprove,
  onReject,
}: {
  requests: LeaveRequest[]
  isLoading: boolean
  error: string | null
  processingId: number | null
  requesterNameById?: Map<number, string>
  onApprove: (id: number) => void
  onReject: (id: number) => void
}) {
  return (
    <section>
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
                <td>{requesterNameById?.get(request.requesterId) ?? `#${request.requesterId}`}</td>
                <td>{LEAVE_TYPE_LABELS[request.type]}</td>
                <td>{request.startDate}</td>
                <td>{request.endDate}</td>
                <td>{request.daysCount}</td>
                <td>{request.reason ?? '—'}</td>
                <td className="actions">
                  <button
                    className="button-primary"
                    disabled={processingId === request.id}
                    onClick={() => onApprove(request.id)}
                  >
                    Zatwierdź
                  </button>
                  <button disabled={processingId === request.id} onClick={() => onReject(request.id)}>
                    Odrzuć
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
