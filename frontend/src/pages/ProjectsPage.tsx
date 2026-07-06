import { useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { createProject, getProjectTimeSummary, listProjects } from '../api/projects'
import { listMyTimeByProject } from '../api/timeEntries'
import { ApiError } from '../api/types'
import type { Project, ProjectTimeSummary } from '../api/types'

function formatMinutes(value: number): string {
  const hours = Math.floor(value / 60)
  const minutes = value % 60
  return `${hours}h ${minutes}min`
}

export function ProjectsPage() {
  const { token, hasAnyRole } = useAuth()
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [createError, setCreateError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  const [mySummary, setMySummary] = useState<ProjectTimeSummary[]>([])

  const [summaries, setSummaries] = useState<Record<number, ProjectTimeSummary | 'error'>>({})
  const [loadingSummaryId, setLoadingSummaryId] = useState<number | null>(null)

  const canManageProjects = hasAnyRole('HR', 'ADMIN')
  const canSeeOrgSummary = hasAnyRole('MANAGER', 'HR', 'ADMIN')

  const loadProjects = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      setProjects(await listProjects(token))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać projektów')
    } finally {
      setIsLoading(false)
    }
  }

  const loadMySummary = async () => {
    if (!token) return
    try {
      setMySummary(await listMyTimeByProject(token))
    } catch {
      // brak własnego podsumowania nie powinien blokować listy projektów
    }
  }

  useEffect(() => {
    loadProjects()
    loadMySummary()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault()
    if (!token) return
    setCreateError(null)
    setIsCreating(true)
    try {
      await createProject({ name, description: description || undefined }, token)
      setName('')
      setDescription('')
      await loadProjects()
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : 'Nie udało się utworzyć projektu')
    } finally {
      setIsCreating(false)
    }
  }

  const handleLoadSummary = async (projectId: number) => {
    if (!token) return
    setLoadingSummaryId(projectId)
    try {
      const summary = await getProjectTimeSummary(projectId, token)
      setSummaries((prev) => ({ ...prev, [projectId]: summary }))
    } catch {
      setSummaries((prev) => ({ ...prev, [projectId]: 'error' }))
    } finally {
      setLoadingSummaryId(null)
    }
  }

  return (
    <div>
      <h1>Projekty</h1>

      {canManageProjects && (
        <div className="centered-form-wrapper">
          <form className="card-form" onSubmit={handleCreate}>
            <h2>Nowy projekt</h2>

            <label htmlFor="projectName">Nazwa</label>
            <input id="projectName" type="text" value={name} onChange={(e) => setName(e.target.value)} required />

            <label htmlFor="projectDescription">Opis (opcjonalnie)</label>
            <input
              id="projectDescription"
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />

            {createError && <p className="auth-error">{createError}</p>}

            <button type="submit" disabled={isCreating}>
              {isCreating ? 'Tworzenie…' : 'Utwórz projekt'}
            </button>
          </form>
        </div>
      )}

      {mySummary.length > 0 && (
        <section className="list-section">
          <h2>Twój czas w projektach</h2>
          <ul className="record-list">
            {mySummary.map((entry) => (
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
        <h2>Wszystkie projekty</h2>

        {error && <p className="auth-error">{error}</p>}

        {isLoading ? (
          <p>Ładowanie…</p>
        ) : projects.length === 0 ? (
          <p>Brak projektów.</p>
        ) : (
          <ul className="record-list">
            {projects.map((project) => {
              const summary = summaries[project.id]
              return (
                <li key={project.id} className="record-list-item record-list-item-column">
                  <div className="record-list-main">
                    <strong>{project.name}</strong>
                    {project.description && <span className="record-list-reason">{project.description}</span>}
                  </div>

                  {canSeeOrgSummary && (
                    <div className="project-summary">
                      {summary && summary !== 'error' ? (
                        <span className="status-badge status-approved">
                          Zespół: {formatMinutes(summary.totalMinutes)} ({summary.entryCount} wpisów)
                        </span>
                      ) : summary === 'error' ? (
                        <span className="auth-error">Nie udało się pobrać podsumowania</span>
                      ) : (
                        <button disabled={loadingSummaryId === project.id} onClick={() => handleLoadSummary(project.id)}>
                          {loadingSummaryId === project.id ? 'Ładowanie…' : 'Pokaż podsumowanie zespołu'}
                        </button>
                      )}
                    </div>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </section>
    </div>
  )
}
