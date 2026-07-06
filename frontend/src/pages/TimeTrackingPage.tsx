import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { clockIn, clockOut, listMyTimeByProject, listMyTimeEntries } from '../api/timeEntries'
import { listProjects } from '../api/projects'
import { ApiError } from '../api/types'
import type { Project, ProjectTimeSummary, TimeEntry } from '../api/types'

function formatDateTime(value: string | null): string {
  return value ? new Date(value).toLocaleString() : '—'
}

function formatMinutes(value: number | null): string {
  if (value === null) return '—'
  const hours = Math.floor(value / 60)
  const minutes = value % 60
  return `${hours}h ${minutes}min`
}

export function TimeTrackingPage() {
  const { token } = useAuth()
  const [entries, setEntries] = useState<TimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [byProject, setByProject] = useState<ProjectTimeSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [selectedProjectId, setSelectedProjectId] = useState('')

  const projectNameById = new Map(projects.map((project) => [project.id, project.name]))

  const loadEntries = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setEntries(await listMyTimeEntries(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać wpisów')
    } finally {
      setIsLoading(false)
    }
  }

  const loadProjects = async () => {
    if (!token) return
    try {
      setProjects(await listProjects(token))
    } catch {
      // brak listy projektów nie blokuje rejestracji czasu bez projektu
    }
  }

  const loadByProject = async () => {
    if (!token) return
    try {
      setByProject(await listMyTimeByProject(token))
    } catch {
      // podsumowanie per projekt jest dodatkiem, nie blokuje reszty strony
    }
  }

  useEffect(() => {
    loadEntries()
    loadProjects()
    loadByProject()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const openEntry = entries.find((entry) => entry.clockOut === null) ?? null

  const handleClockIn = async () => {
    if (!token) return
    setError(null)
    setIsSubmitting(true)
    try {
      await clockIn(token, selectedProjectId ? Number(selectedProjectId) : undefined)
      await loadEntries()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się rozpocząć pracy')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleClockOut = async () => {
    if (!token) return
    setError(null)
    setIsSubmitting(true)
    try {
      await clockOut(token)
      await loadEntries()
      await loadByProject()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się zakończyć pracy')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Czas pracy</h1>

      <div className="centered-form-wrapper">
        <div className="card-form">
          <h2>Rejestracja czasu pracy</h2>

          {openEntry ? (
            <p>
              Pracujesz od <strong>{formatDateTime(openEntry.clockIn)}</strong>
              {openEntry.projectId && (
                <>
                  {' '}
                  nad projektem <strong>{projectNameById.get(openEntry.projectId) ?? `#${openEntry.projectId}`}</strong>
                </>
              )}
            </p>
          ) : (
            <>
              <p>Nie masz obecnie otwartego wpisu czasu pracy.</p>
              <label htmlFor="projectSelect">Projekt (opcjonalnie)</label>
              <select
                id="projectSelect"
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
              >
                <option value="">Brak projektu</option>
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.name}
                  </option>
                ))}
              </select>
            </>
          )}

          {error && <p className="auth-error">{error}</p>}

          {openEntry ? (
            <button disabled={isSubmitting} onClick={handleClockOut}>
              {isSubmitting ? 'Zapisywanie…' : 'Zakończ pracę'}
            </button>
          ) : (
            <button disabled={isSubmitting} onClick={handleClockIn}>
              {isSubmitting ? 'Zapisywanie…' : 'Rozpocznij pracę'}
            </button>
          )}
        </div>
      </div>

      {byProject.length > 0 && (
        <section className="list-section">
          <h2>Twój czas w projektach</h2>
          <ul className="record-list">
            {byProject.map((entry) => (
              <li key={entry.projectId} className="record-list-item">
                <div className="record-list-main">
                  <strong>{entry.projectName}</strong>
                  <span>{entry.entryCount} wpisów</span>
                </div>
                <span className="status-badge status-approved">{formatMinutes(entry.totalMinutes)}</span>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="list-section">
        <h2>Twoje wpisy</h2>

        {isLoading ? (
          <p>Ładowanie…</p>
        ) : entries.length === 0 ? (
          <p>Brak zarejestrowanych wpisów czasu pracy.</p>
        ) : (
          <ul className="record-list">
            {entries.map((entry) => (
              <li key={entry.id} className="record-list-item">
                <div className="record-list-main">
                  <strong>{formatDateTime(entry.clockIn)}</strong>
                  <span>do {formatDateTime(entry.clockOut)}</span>
                  {entry.projectId && (
                    <span className="record-list-reason">
                      Projekt: {projectNameById.get(entry.projectId) ?? `#${entry.projectId}`}
                    </span>
                  )}
                  {entry.breakMinutes > 0 && <span className="record-list-reason">Przerwa: {entry.breakMinutes} min</span>}
                </div>
                <span className="status-badge status-approved">{formatMinutes(entry.totalMinutes)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
