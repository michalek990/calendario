import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { listFacilities } from '../api/facilities'
import { listAllUsers, updateUserOrganization, updateUserRole } from '../api/users'
import { ApiError } from '../api/types'
import type { Facility, UserProfile } from '../api/types'
import { ROLE_LABELS } from '../auth/jwt'
import type { Role } from '../auth/jwt'

const ROLE_OPTIONS: Role[] = ['EMPLOYEE', 'MANAGER', 'HR', 'ADMIN']

function initialsOf(user: UserProfile): string {
  return `${user.firstName[0] ?? ''}${user.lastName[0] ?? ''}`.toUpperCase()
}

interface EditForm {
  role: Role
  position: string
  department: string
  facility: string
  supervisorId: string
}

export function AdminUserEditPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const userId = Number(id)

  const [users, setUsers] = useState<UserProfile[]>([])
  const [facilities, setFacilities] = useState<Facility[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [form, setForm] = useState<EditForm | null>(null)
  const [isSaving, setIsSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    setIsLoading(true)
    Promise.all([listAllUsers(token), listFacilities(token)])
      .then(([allUsers, allFacilities]) => {
        setUsers(allUsers)
        setFacilities(allFacilities)
        const target = allUsers.find((u) => u.id === userId)
        if (!target) {
          setLoadError('Nie znaleziono użytkownika')
          return
        }
        setForm({
          role: target.role,
          position: target.position ?? '',
          department: target.department ?? '',
          facility: target.facility ?? '',
          supervisorId: target.supervisorId ? String(target.supervisorId) : '',
        })
      })
      .catch((err) => {
        setLoadError(err instanceof ApiError ? err.message : 'Nie udało się pobrać danych użytkownika')
      })
      .finally(() => setIsLoading(false))
  }, [token, userId])

  const user = users.find((u) => u.id === userId)

  const handleSave = async (event: FormEvent) => {
    event.preventDefault()
    if (!token || !form || !user) return
    setIsSaving(true)
    setSaveError(null)
    try {
      await updateUserOrganization(
        user.id,
        {
          position: form.position || null,
          department: form.department || null,
          facility: form.facility || null,
          supervisorId: form.supervisorId ? Number(form.supervisorId) : null,
        },
        token,
      )
      if (form.role !== user.role) {
        await updateUserRole(user.id, { role: form.role }, token)
      }
      navigate('/admin/users')
    } catch (err) {
      setSaveError(err instanceof ApiError ? err.message : 'Nie udało się zapisać zmian')
    } finally {
      setIsSaving(false)
    }
  }

  if (isLoading) {
    return (
      <div>
        <h1>Edycja użytkownika</h1>
        <p>Ładowanie…</p>
      </div>
    )
  }

  if (loadError || !user || !form) {
    return (
      <div>
        <h1>Edycja użytkownika</h1>
        <p className="auth-error">{loadError ?? 'Nie znaleziono użytkownika'}</p>
        <button onClick={() => navigate('/admin/users')}>Wróć do listy</button>
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1>Edycja użytkownika</h1>
        <p className="page-subtitle">Rola, przynależność organizacyjna i przełożony.</p>
      </div>

      <section className="settings-section">
        <div className="person-cell" style={{ marginBottom: 20 }}>
          <span className="person-avatar">{initialsOf(user)}</span>
          <div>
            <div className="person-name">
              {user.firstName} {user.lastName}
            </div>
            <div className="person-email">{user.email}</div>
          </div>
        </div>

        <form className="wide-form" onSubmit={handleSave}>
          <div>
            <label htmlFor="edit-role">Rola</label>
            <select
              id="edit-role"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
            >
              {ROLE_OPTIONS.map((role) => (
                <option key={role} value={role}>
                  {ROLE_LABELS[role]}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="edit-position">Stanowisko</label>
            <input
              id="edit-position"
              type="text"
              placeholder="—"
              value={form.position}
              onChange={(e) => setForm({ ...form, position: e.target.value })}
            />
          </div>

          <div>
            <label htmlFor="edit-department">Dział</label>
            <input
              id="edit-department"
              type="text"
              placeholder="—"
              value={form.department}
              onChange={(e) => setForm({ ...form, department: e.target.value })}
            />
          </div>

          <div>
            <label htmlFor="edit-facility">Zakład</label>
            <select
              id="edit-facility"
              value={form.facility}
              onChange={(e) => setForm({ ...form, facility: e.target.value })}
            >
              <option value="">Brak</option>
              {form.facility && !facilities.some((f) => f.name === form.facility) && (
                <option value={form.facility}>{form.facility} (nieznany zakład)</option>
              )}
              {facilities.map((facility) => (
                <option key={facility.id} value={facility.name}>
                  {facility.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="edit-supervisor">Przełożony</label>
            <select
              id="edit-supervisor"
              value={form.supervisorId}
              onChange={(e) => setForm({ ...form, supervisorId: e.target.value })}
            >
              <option value="">Brak</option>
              {users
                .filter((candidate) => candidate.id !== user.id)
                .map((candidate) => (
                  <option key={candidate.id} value={candidate.id}>
                    {candidate.firstName} {candidate.lastName}
                  </option>
                ))}
            </select>
          </div>

          {saveError && <p className="auth-error">{saveError}</p>}

          <div className="inline-edit-actions">
            <button type="submit" disabled={isSaving}>
              {isSaving ? 'Zapisywanie…' : 'Zapisz'}
            </button>
            <button
              type="button"
              className="button-secondary"
              disabled={isSaving}
              onClick={() => navigate('/admin/users')}
            >
              Anuluj
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
