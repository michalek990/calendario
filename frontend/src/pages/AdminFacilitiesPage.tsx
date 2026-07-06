import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { approveLeaveRequest, listPendingLeaveRequests, rejectLeaveRequest } from '../api/leave'
import { listManagedTimeEntries } from '../api/timeEntries'
import { listProjects } from '../api/projects'
import { listAllUsers } from '../api/users'
import { ApiError } from '../api/types'
import type { LeaveRequest, ManagedTimeEntry, Project, UserProfile } from '../api/types'
import { LeaveApprovalTable } from '../components/LeaveApprovalTable'
import { ManagedTimeEntriesList } from '../components/ManagedTimeEntriesList'

export function AdminFacilitiesPage() {
  const { token } = useAuth()
  const [activeTab, setActiveTab] = useState<'requests' | 'time'>('requests')
  const [selectedFacility, setSelectedFacility] = useState<string>('')

  const [users, setUsers] = useState<UserProfile[]>([])
  const [isLoadingUsers, setIsLoadingUsers] = useState(true)
  const [usersError, setUsersError] = useState<string | null>(null)

  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoadingRequests, setIsLoadingRequests] = useState(true)
  const [requestsError, setRequestsError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const [entries, setEntries] = useState<ManagedTimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoadingEntries, setIsLoadingEntries] = useState(true)
  const [entriesError, setEntriesError] = useState<string | null>(null)

  const loadUsers = async () => {
    if (!token) return
    setIsLoadingUsers(true)
    try {
      setUsers(await listAllUsers(token))
    } catch (err) {
      setUsersError(err instanceof ApiError ? err.message : 'Nie udało się pobrać listy pracowników')
    } finally {
      setIsLoadingUsers(false)
    }
  }

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
      setEntriesError(err instanceof ApiError ? err.message : 'Nie udało się pobrać czasu pracy')
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

    loadUsers()
    loadRequests()
    loadEntries()
    loadProjects()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const facilities = useMemo(() => {
    const values = new Set(users.map((u) => u.facility).filter((f): f is string => !!f))
    return Array.from(values).sort((a, b) => a.localeCompare(b))
  }, [users])

  useEffect(() => {
    if (!selectedFacility && facilities.length > 0) {
      setSelectedFacility(facilities[0])
    }
  }, [facilities, selectedFacility])

  const facilityByUserId = useMemo(() => new Map(users.map((u) => [u.id, u.facility])), [users])
  const nameByUserId = useMemo(
    () => new Map(users.map((u) => [u.id, `${u.firstName} ${u.lastName}`])),
    [users],
  )

  const filteredRequests = useMemo(
    () => requests.filter((r) => facilityByUserId.get(r.requesterId) === selectedFacility),
    [requests, facilityByUserId, selectedFacility],
  )

  const filteredEntries = useMemo(
    () => entries.filter((e) => facilityByUserId.get(e.userId) === selectedFacility),
    [entries, facilityByUserId, selectedFacility],
  )

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
        <h1>Zakłady</h1>
        <p className="page-subtitle">Wnioski urlopowe i czas pracy pracowników wybranego zakładu.</p>
      </div>

      {usersError && <p className="auth-error">{usersError}</p>}

      {isLoadingUsers ? (
        <p>Ładowanie…</p>
      ) : facilities.length === 0 ? (
        <p>Żaden pracownik nie ma jeszcze przypisanego zakładu.</p>
      ) : (
        <>
          <div className="wide-form" style={{ marginBottom: 20, maxWidth: 320 }}>
            <div>
              <label htmlFor="facility-select">Zakład</label>
              <select
                id="facility-select"
                value={selectedFacility}
                onChange={(e) => setSelectedFacility(e.target.value)}
              >
                {facilities.map((facility) => (
                  <option key={facility} value={facility}>
                    {facility}
                  </option>
                ))}
              </select>
            </div>
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
              Czas pracy
            </button>
          </div>

          {activeTab === 'requests' ? (
            <LeaveApprovalTable
              requests={filteredRequests}
              isLoading={isLoadingRequests}
              error={requestsError}
              processingId={processingId}
              requesterNameById={nameByUserId}
              onApprove={(id) => handleDecision(id, approveLeaveRequest)}
              onReject={(id) => handleDecision(id, rejectLeaveRequest)}
            />
          ) : (
            <ManagedTimeEntriesList
              entries={filteredEntries}
              projects={projects}
              isLoading={isLoadingEntries}
              error={entriesError}
              token={token ?? ''}
              onEntryUpdated={loadEntries}
            />
          )}
        </>
      )}
    </div>
  )
}
