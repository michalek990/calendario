import { useState, type FormEvent } from 'react'
import { updateTimeEntry } from '../api/timeEntries'
import { ApiError } from '../api/types'
import type { ManagedTimeEntry, Project } from '../api/types'

function toIsoDateInput(iso: string): string {
  return new Date(iso).toISOString().slice(0, 10)
}

function toTimeInput(iso: string): string {
  const date = new Date(iso)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

/** Łączy datę i godzinę wpisane przez HR/przełożonego (w ich lokalnej strefie czasowej) w chwilę UTC. */
function toInstant(dateValue: string, timeValue: string): string {
  return new Date(`${dateValue}T${timeValue}:00`).toISOString()
}

function formatEntryDate(iso: string): string {
  return new Date(iso).toLocaleDateString()
}

function formatEntryTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatMinutes(value: number | null): string {
  if (value === null) return '—'
  const hours = Math.floor(value / 60)
  const minutes = value % 60
  return `${hours}h ${minutes}min`
}

function initialsOf(entry: ManagedTimeEntry): string {
  return `${entry.userFirstName?.[0] ?? ''}${entry.userLastName?.[0] ?? ''}`.toUpperCase()
}

interface EditForm {
  date: string
  startTime: string
  endTime: string
  breakMinutes: string
  projectId: string
}

export function ManagedTimeEntriesList({
  entries,
  projects,
  isLoading,
  error,
  token,
  onEntryUpdated,
}: {
  entries: ManagedTimeEntry[]
  projects: Project[]
  isLoading: boolean
  error: string | null
  token: string
  onEntryUpdated: () => void
}) {
  const [editingEntryId, setEditingEntryId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<EditForm | null>(null)
  const [isSaving, setIsSaving] = useState(false)
  const [editError, setEditError] = useState<string | null>(null)

  const projectNameById = new Map(projects.map((project) => [project.id, project.name]))

  const startEditing = (entry: ManagedTimeEntry) => {
    setEditingEntryId(entry.id)
    setEditError(null)
    setEditForm({
      date: toIsoDateInput(entry.clockIn),
      startTime: toTimeInput(entry.clockIn),
      endTime: entry.clockOut ? toTimeInput(entry.clockOut) : '',
      breakMinutes: entry.breakMinutes > 0 ? String(entry.breakMinutes) : '',
      projectId: entry.projectId ? String(entry.projectId) : '',
    })
  }

  const cancelEditing = () => {
    setEditingEntryId(null)
    setEditForm(null)
    setEditError(null)
  }

  const handleSaveEdit = async (event: FormEvent) => {
    event.preventDefault()
    if (editingEntryId === null || !editForm) return
    setEditError(null)
    setIsSaving(true)
    try {
      await updateTimeEntry(
        editingEntryId,
        {
          clockIn: toInstant(editForm.date, editForm.startTime),
          clockOut: editForm.endTime ? toInstant(editForm.date, editForm.endTime) : null,
          breakMinutes: editForm.breakMinutes ? Number(editForm.breakMinutes) : undefined,
          projectId: editForm.projectId ? Number(editForm.projectId) : undefined,
        },
        token,
      )
      cancelEditing()
      onEntryUpdated()
    } catch (err) {
      setEditError(err instanceof ApiError ? err.message : 'Nie udało się zapisać poprawki')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section>
      {error && <p className="auth-error">{error}</p>}

      {isLoading ? (
        <p>Ładowanie…</p>
      ) : entries.length === 0 ? (
        <p>Brak zarejestrowanych wpisów czasu pracy.</p>
      ) : (
        <ul className="record-list">
          {entries.map((entry) =>
            editingEntryId === entry.id && editForm ? (
              <li key={entry.id} className="record-list-item record-list-item-editing">
                <form className="inline-edit-form" onSubmit={handleSaveEdit}>
                  <label htmlFor={`date-${entry.id}`}>Dzień</label>
                  <input
                    id={`date-${entry.id}`}
                    type="date"
                    value={editForm.date}
                    onChange={(e) => setEditForm({ ...editForm, date: e.target.value })}
                    required
                  />

                  <label htmlFor={`start-${entry.id}`}>Od godziny</label>
                  <input
                    id={`start-${entry.id}`}
                    type="time"
                    value={editForm.startTime}
                    onChange={(e) => setEditForm({ ...editForm, startTime: e.target.value })}
                    required
                  />

                  <label htmlFor={`end-${entry.id}`}>Do godziny</label>
                  <input
                    id={`end-${entry.id}`}
                    type="time"
                    value={editForm.endTime}
                    onChange={(e) => setEditForm({ ...editForm, endTime: e.target.value })}
                  />

                  <label htmlFor={`break-${entry.id}`}>Przerwa (min)</label>
                  <input
                    id={`break-${entry.id}`}
                    type="number"
                    min={0}
                    value={editForm.breakMinutes}
                    onChange={(e) => setEditForm({ ...editForm, breakMinutes: e.target.value })}
                  />

                  <label htmlFor={`project-${entry.id}`}>Projekt</label>
                  <select
                    id={`project-${entry.id}`}
                    value={editForm.projectId}
                    onChange={(e) => setEditForm({ ...editForm, projectId: e.target.value })}
                  >
                    <option value="">Brak projektu</option>
                    {projects.map((project) => (
                      <option key={project.id} value={project.id}>
                        {project.name}
                      </option>
                    ))}
                  </select>

                  {editError && <p className="auth-error">{editError}</p>}

                  <div className="inline-edit-actions">
                    <button type="submit" disabled={isSaving}>
                      {isSaving ? 'Zapisywanie…' : 'Zapisz'}
                    </button>
                    <button type="button" className="button-secondary" onClick={cancelEditing} disabled={isSaving}>
                      Anuluj
                    </button>
                  </div>
                </form>
              </li>
            ) : (
              <li key={entry.id} className="record-list-item">
                <div className="person-cell">
                  <span className="person-avatar">{initialsOf(entry)}</span>
                  <div className="record-list-main">
                    <strong>
                      {entry.userFirstName} {entry.userLastName}
                    </strong>
                    <span>
                      {formatEntryDate(entry.clockIn)} · {formatEntryTime(entry.clockIn)} –{' '}
                      {entry.clockOut ? formatEntryTime(entry.clockOut) : '…'}
                    </span>
                    {entry.projectId && (
                      <span className="record-list-reason">
                        Projekt: {projectNameById.get(entry.projectId) ?? `#${entry.projectId}`}
                      </span>
                    )}
                  </div>
                </div>
                <span className="status-badge status-approved">{formatMinutes(entry.totalMinutes)}</span>
                <button type="button" className="button-secondary" onClick={() => startEditing(entry)}>
                  Edytuj
                </button>
              </li>
            ),
          )}
        </ul>
      )}
    </section>
  )
}
