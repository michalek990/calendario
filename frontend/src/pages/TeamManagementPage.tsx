import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { approveLeaveRequest, listPendingLeaveRequests, rejectLeaveRequest } from '../api/leave'
import { listManagedTimeEntries } from '../api/timeEntries'
import { listProjects } from '../api/projects'
import { ApiError } from '../api/types'
import type { LeaveRequest, ManagedTimeEntry, Project } from '../api/types'
import { LeaveApprovalTable } from '../components/LeaveApprovalTable'
import { ManagedTimeEntriesList } from '../components/ManagedTimeEntriesList'

export function TeamManagementPage() {
  const { token } = useAuth()
  const [activeTab, setActiveTab] = useState<'requests' | 'time'>('requests')

  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoadingRequests, setIsLoadingRequests] = useState(true)
  const [requestsError, setRequestsError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const [entries, setEntries] = useState<ManagedTimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoadingEntries, setIsLoadingEntries] = useState(true)
  const [entriesError, setEntriesError] = useState<string | null>(null)

  const loadRequests = async () => {
    if (!token) return
    setIsLoadingRequests(true)
    try {
      setRequests(await listPendingLeaveRequests(token))
    } catch (err) {
      setRequestsError(err instanceof ApiError ? err.message : 'Nie udało się pobrać wniosków')
    } finally {
      setIsLoadingRequests(false)
    }
  }

  const loadEntries = async () => {
    if (!token) return
    setIsLoadingEntries(true)
    try {
      setEntries(await listManagedTimeEntries(token))
    } catch (err) {
      setEntriesError(err instanceof ApiError ? err.message : 'Nie udało się pobrać czasu pracy zespołu')
    } finally {
      setIsLoadingEntries(false)
    }
  }

  useEffect(() => {
    if (!token) return

    async function loadProjects() {
      try {
        setProjects(await listProjects(token as string))
      } catch {
        // brak listy projektów nie blokuje wyświetlenia wpisów
      }
    }

    loadRequests()
    loadEntries()
    loadProjects()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleDecision = async (id: number, decide: (id: number, token: string) => Promise<LeaveRequest>) => {
    if (!token) return
    setRequestsError(null)
    setProcessingId(id)
    try {
      await decide(id, token)
      await loadRequests()
    } catch (err) {
      setRequestsError(err instanceof ApiError ? err.message : 'Nie udało się zapisać decyzji')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Zespół</h1>
        <p className="page-subtitle">Wnioski urlopowe i czas pracy podległych pracowników.</p>
      </div>

      <div className="section-tabs">
        <button
          type="button"
          className={`section-tab ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          Wnioski do zatwierdzenia
        </button>
        <button
          type="button"
          className={`section-tab ${activeTab === 'time' ? 'active' : ''}`}
          onClick={() => setActiveTab('time')}
        >
          Czas pracy zespołu
        </button>
      </div>

      {activeTab === 'requests' ? (
        <LeaveApprovalTable
          requests={requests}
          isLoading={isLoadingRequests}
          error={requestsError}
          processingId={processingId}
          onApprove={(id) => handleDecision(id, approveLeaveRequest)}
          onReject={(id) => handleDecision(id, rejectLeaveRequest)}
        />
      ) : (
        <ManagedTimeEntriesList
          entries={entries}
          projects={projects}
          isLoading={isLoadingEntries}
          error={entriesError}
          token={token ?? ''}
          onEntryUpdated={loadEntries}
        />
      )}
    </div>
  )
}
