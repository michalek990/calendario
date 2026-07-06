import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { approveLeaveRequest, listPendingLeaveRequests, rejectLeaveRequest } from '../api/leave'
import { listManagedTimeEntries } from '../api/timeEntries'
import { listProjects } from '../api/projects'
import { listAllUsers } from '../api/users'
import { createFacility, deleteFacility, listFacilities, updateFacility } from '../api/facilities'
import { ApiError } from '../api/types'
import type { Facility, LeaveRequest, ManagedTimeEntry, Project, UserProfile } from '../api/types'
import { LeaveApprovalTable } from '../components/LeaveApprovalTable'
import { ManagedTimeEntriesList } from '../components/ManagedTimeEntriesList'

type Tab = 'requests' | 'time' | 'manage'

export function AdminFacilitiesPage() {
  const { token } = useAuth()
  const [activeTab, setActiveTab] = useState<Tab>('requests')
  const [selectedFacility, setSelectedFacility] = useState<string>('')

  const [users, setUsers] = useState<UserProfile[]>([])
  const [isLoadingUsers, setIsLoadingUsers] = useState(true)
  const [usersError, setUsersError] = useState<string | null>(null)

  const [facilities, setFacilities] = useState<Facility[]>([])
  const [isLoadingFacilities, setIsLoadingFacilities] = useState(true)
  const [facilitiesError, setFacilitiesError] = useState<string | null>(null)

  const [requests, setRequests] = useState<LeaveRequest[]>([])
  const [isLoadingRequests, setIsLoadingRequests] = useState(true)
  const [requestsError, setRequestsError] = useState<string | null>(null)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const [entries, setEntries] = useState<ManagedTimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoadingEntries, setIsLoadingEntries] = useState(true)
  const [entriesError, setEntriesError] = useState<string | null>(null)

  const [newFacilityName, setNewFacilityName] = useState('')
  const [isCreatingFacility, setIsCreatingFacility] = useState(false)
  const [createFacilityError, setCreateFacilityError] = useState<string | null>(null)

  const [editingFacilityId, setEditingFacilityId] = useState<number | null>(null)
  const [editFacilityName, setEditFacilityName] = useState('')
  const [isSavingFacility, setIsSavingFacility] = useState(false)
  const [facilityRowError, setFacilityRowError] = useState<string | null>(null)

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

  const loadFacilities = async () => {
    if (!token) return
    setIsLoadingFacilities(true)
    try {
      setFacilities(await listFacilities(token))
    } catch (err) {
      setFacilitiesError(err instanceof ApiError ? err.message : 'Nie udało się pobrać listy zakładów')
    } finally {
      setIsLoadingFacilities(false)
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
    loadFacilities()
    loadRequests()
    loadEntries()
    loadProjects()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const facilityNames = useMemo(
    () => facilities.map((f) => f.name).sort((a, b) => a.localeCompare(b)),
    [facilities],
  )

  useEffect(() => {
    if ((!selectedFacility || !facilityNames.includes(selectedFacility)) && facilityNames.length > 0) {
      setSelectedFacility(facilityNames[0])
    }
  }, [facilityNames, selectedFacility])

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

  const handleCreateFacility = async (event: FormEvent) => {
    event.preventDefault()
    if (!token || !newFacilityName.trim()) return
    setIsCreatingFacility(true)
    setCreateFacilityError(null)
    try {
      await createFacility(newFacilityName.trim(), token)
      setNewFacilityName('')
      await loadFacilities()
    } catch (err) {
      setCreateFacilityError(err instanceof ApiError ? err.message : 'Nie udało się utworzyć zakładu')
    } finally {
      setIsCreatingFacility(false)
    }
  }

  const startEditingFacility = (facility: Facility) => {
    setEditingFacilityId(facility.id)
    setEditFacilityName(facility.name)
    setFacilityRowError(null)
  }

  const cancelEditingFacility = () => {
    setEditingFacilityId(null)
    setEditFacilityName('')
    setFacilityRowError(null)
  }

  const handleRenameFacility = async (event: FormEvent) => {
    event.preventDefault()
    if (!token || editingFacilityId === null || !editFacilityName.trim()) return
    setIsSavingFacility(true)
    setFacilityRowError(null)
    try {
      await updateFacility(editingFacilityId, editFacilityName.trim(), token)
      cancelEditingFacility()
      await Promise.all([loadFacilities(), loadUsers()])
    } catch (err) {
      setFacilityRowError(err instanceof ApiError ? err.message : 'Nie udało się zmienić nazwy zakładu')
    } finally {
      setIsSavingFacility(false)
    }
  }

  const handleDeleteFacility = async (facility: Facility) => {
    if (!token) return
    setFacilityRowError(null)
    try {
      await deleteFacility(facility.id, token)
      await loadFacilities()
    } catch (err) {
      setFacilitiesError(err instanceof ApiError ? err.message : 'Nie udało się usunąć zakładu')
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Zakłady</h1>
        <p className="page-subtitle">Wnioski urlopowe, czas pracy i lista zakładów.</p>
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
        <button
          type="button"
          className={`section-tab ${activeTab === 'manage' ? 'active' : ''}`}
          onClick={() => setActiveTab('manage')}
        >
          Zarządzanie zakładami
        </button>
      </div>

      {activeTab === 'manage' ? (
        <section>
          {facilitiesError && <p className="auth-error">{facilitiesError}</p>}

          <form className="wide-form" style={{ marginBottom: 20, maxWidth: 420 }} onSubmit={handleCreateFacility}>
            <div>
              <label htmlFor="new-facility-name">Nowy zakład</label>
              <input
                id="new-facility-name"
                type="text"
                placeholder="np. Warszawa"
                value={newFacilityName}
                onChange={(e) => setNewFacilityName(e.target.value)}
                required
              />
            </div>
            <div>
              <button type="submit" className="button-primary" disabled={isCreatingFacility}>
                {isCreatingFacility ? 'Dodawanie…' : 'Dodaj zakład'}
              </button>
            </div>
          </form>
          {createFacilityError && <p className="auth-error">{createFacilityError}</p>}

          {isLoadingFacilities ? (
            <p>Ładowanie…</p>
          ) : facilities.length === 0 ? (
            <p>Brak zakładów — dodaj pierwszy powyżej.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Nazwa</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {facilities
                  .slice()
                  .sort((a, b) => a.name.localeCompare(b.name))
                  .map((facility) =>
                    editingFacilityId === facility.id ? (
                      <tr key={facility.id} className="table-row-editing">
                        <td>
                          <form id={`rename-form-${facility.id}`} onSubmit={handleRenameFacility}>
                            <input
                              className="table-input"
                              type="text"
                              value={editFacilityName}
                              onChange={(e) => setEditFacilityName(e.target.value)}
                              required
                            />
                          </form>
                        </td>
                        <td className="actions">
                          <button
                            type="submit"
                            form={`rename-form-${facility.id}`}
                            className="button-primary"
                            disabled={isSavingFacility}
                          >
                            {isSavingFacility ? 'Zapisywanie…' : 'Zapisz'}
                          </button>
                          <button type="button" disabled={isSavingFacility} onClick={cancelEditingFacility}>
                            Anuluj
                          </button>
                        </td>
                      </tr>
                    ) : (
                      <tr key={facility.id}>
                        <td>{facility.name}</td>
                        <td className="actions">
                          <button onClick={() => startEditingFacility(facility)}>Edytuj</button>
                          <button onClick={() => handleDeleteFacility(facility)}>Usuń</button>
                        </td>
                      </tr>
                    ),
                  )}
                {facilityRowError && (
                  <tr className="table-row-editing">
                    <td colSpan={2}>
                      <p className="auth-error">{facilityRowError}</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </section>
      ) : (
        <>
          {usersError && <p className="auth-error">{usersError}</p>}

          {facilityNames.length === 0 ? (
            <p>Brak zakładów — dodaj pierwszy w zakładce "Zarządzanie zakładami".</p>
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
                    {facilityNames.map((facility) => (
                      <option key={facility} value={facility}>
                        {facility}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {activeTab === 'requests' ? (
                <LeaveApprovalTable
                  requests={filteredRequests}
                  isLoading={isLoadingRequests || isLoadingUsers}
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
                  isLoading={isLoadingEntries || isLoadingUsers}
                  error={entriesError}
                  token={token ?? ''}
                  onEntryUpdated={loadEntries}
                />
              )}
            </>
          )}
        </>
      )}
    </div>
  )
}
