import { useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { ROLE_LABELS } from '../auth/jwt'
import { getMyProfile, updateMyPersonalInfo, updateUserOrganization } from '../api/users'
import { ApiError } from '../api/types'
import type { UserProfile } from '../api/types'

function formatDateTime(value: string | null): string {
  return value ? new Date(value).toLocaleString() : '—'
}

export function ProfilePage() {
  const { token, hasAnyRole } = useAuth()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [birthDate, setBirthDate] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [personalError, setPersonalError] = useState<string | null>(null)
  const [personalSuccess, setPersonalSuccess] = useState<string | null>(null)
  const [isSavingPersonal, setIsSavingPersonal] = useState(false)

  const [orgUserId, setOrgUserId] = useState('')
  const [orgPosition, setOrgPosition] = useState('')
  const [orgDepartment, setOrgDepartment] = useState('')
  const [orgFacility, setOrgFacility] = useState('')
  const [orgSupervisorId, setOrgSupervisorId] = useState('')
  const [orgError, setOrgError] = useState<string | null>(null)
  const [orgSuccess, setOrgSuccess] = useState<string | null>(null)
  const [isSavingOrg, setIsSavingOrg] = useState(false)

  const loadProfile = async () => {
    if (!token) return
    setIsLoading(true)
    try {
      const data = await getMyProfile(token)
      setProfile(data)
      setBirthDate(data.birthDate ?? '')
      setPhoneNumber(data.phoneNumber ?? '')
      setAvatarUrl(data.avatarUrl ?? '')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać profilu')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadProfile()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  const handleSavePersonalInfo = async (event: FormEvent) => {
    event.preventDefault()
    if (!token) return
    setPersonalError(null)
    setPersonalSuccess(null)
    setIsSavingPersonal(true)
    try {
      const updated = await updateMyPersonalInfo(
        {
          birthDate: birthDate || null,
          phoneNumber: phoneNumber || null,
          avatarUrl: avatarUrl || null,
        },
        token,
      )
      setProfile(updated)
      setPersonalSuccess('Dane zapisane')
    } catch (err) {
      setPersonalError(err instanceof ApiError ? err.message : 'Nie udało się zapisać danych')
    } finally {
      setIsSavingPersonal(false)
    }
  }

  const handleSaveOrganization = async (event: FormEvent) => {
    event.preventDefault()
    if (!token) return
    setOrgError(null)
    setOrgSuccess(null)
    const userId = Number(orgUserId)
    if (!userId) {
      setOrgError('Podaj poprawne id pracownika')
      return
    }
    setIsSavingOrg(true)
    try {
      await updateUserOrganization(
        userId,
        {
          position: orgPosition || null,
          department: orgDepartment || null,
          facility: orgFacility || null,
          supervisorId: orgSupervisorId ? Number(orgSupervisorId) : null,
        },
        token,
      )
      setOrgSuccess(`Zaktualizowano dane organizacyjne pracownika #${userId}`)
    } catch (err) {
      setOrgError(err instanceof ApiError ? err.message : 'Nie udało się zapisać danych')
    } finally {
      setIsSavingOrg(false)
    }
  }

  if (isLoading) {
    return (
      <div>
        <h1>Profil</h1>
        <p>Ładowanie…</p>
      </div>
    )
  }

  if (error || !profile) {
    return (
      <div>
        <h1>Profil</h1>
        <p className="auth-error">{error ?? 'Nie udało się wczytać profilu'}</p>
      </div>
    )
  }

  return (
    <div>
      <h1>Profil</h1>

      <section className="settings-section">
        <h2>
          {profile.firstName} {profile.lastName}
        </h2>
        <div className="profile-grid">
          <div>
            <span className="profile-label">E-mail</span>
            <span>{profile.email}</span>
          </div>
          <div>
            <span className="profile-label">Rola</span>
            <span>{ROLE_LABELS[profile.role] ?? profile.role}</span>
          </div>
          <div>
            <span className="profile-label">Stanowisko</span>
            <span>{profile.position ?? '—'}</span>
          </div>
          <div>
            <span className="profile-label">Dział</span>
            <span>{profile.department ?? '—'}</span>
          </div>
          <div>
            <span className="profile-label">Zakład</span>
            <span>{profile.facility ?? '—'}</span>
          </div>
          <div>
            <span className="profile-label">Przełożony</span>
            <span>{profile.hasSupervisor ? profile.supervisorFullName : '—'}</span>
          </div>
          <div>
            <span className="profile-label">Czy jest przełożonym</span>
            <span>{profile.isSupervisor ? 'Tak' : 'Nie'}</span>
          </div>
          <div>
            <span className="profile-label">Ostatnie logowanie</span>
            <span>{formatDateTime(profile.lastLoginAt)}</span>
          </div>
        </div>
      </section>

      <section className="settings-section">
        <h2>Twoje dane osobowe</h2>
        <form className="settings-form" onSubmit={handleSavePersonalInfo}>
          <label htmlFor="birthDate">Data urodzenia</label>
          <input
            id="birthDate"
            type="date"
            value={birthDate}
            onChange={(e) => setBirthDate(e.target.value)}
          />

          <label htmlFor="phoneNumber">Telefon kontaktowy</label>
          <input
            id="phoneNumber"
            type="tel"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />

          <label htmlFor="avatarUrl">URL awatara</label>
          <input
            id="avatarUrl"
            type="url"
            value={avatarUrl}
            onChange={(e) => setAvatarUrl(e.target.value)}
            placeholder="https://..."
          />

          {avatarUrl && (
            <img src={avatarUrl} alt="Podgląd awatara" className="avatar-preview" />
          )}

          {personalError && <p className="auth-error">{personalError}</p>}
          {personalSuccess && <p className="settings-success">{personalSuccess}</p>}

          <button type="submit" disabled={isSavingPersonal}>
            {isSavingPersonal ? 'Zapisywanie…' : 'Zapisz'}
          </button>
        </form>
      </section>

      {hasAnyRole('HR', 'ADMIN') && (
        <section className="settings-section">
          <h2>Dane organizacyjne pracownika (HR/ADMIN)</h2>
          <p className="profile-hint">Podaj id pracownika — profil nie zawiera jeszcze listy wszystkich osób.</p>
          <form className="settings-form" onSubmit={handleSaveOrganization}>
            <label htmlFor="orgUserId">Id pracownika</label>
            <input
              id="orgUserId"
              type="number"
              value={orgUserId}
              onChange={(e) => setOrgUserId(e.target.value)}
              required
            />

            <label htmlFor="orgPosition">Stanowisko</label>
            <input id="orgPosition" type="text" value={orgPosition} onChange={(e) => setOrgPosition(e.target.value)} />

            <label htmlFor="orgDepartment">Dział</label>
            <input
              id="orgDepartment"
              type="text"
              value={orgDepartment}
              onChange={(e) => setOrgDepartment(e.target.value)}
            />

            <label htmlFor="orgFacility">Zakład</label>
            <input id="orgFacility" type="text" value={orgFacility} onChange={(e) => setOrgFacility(e.target.value)} />

            <label htmlFor="orgSupervisorId">Id przełożonego (opcjonalnie)</label>
            <input
              id="orgSupervisorId"
              type="number"
              value={orgSupervisorId}
              onChange={(e) => setOrgSupervisorId(e.target.value)}
            />

            {orgError && <p className="auth-error">{orgError}</p>}
            {orgSuccess && <p className="settings-success">{orgSuccess}</p>}

            <button type="submit" disabled={isSavingOrg}>
              {isSavingOrg ? 'Zapisywanie…' : 'Zapisz dane organizacyjne'}
            </button>
          </form>
        </section>
      )}
    </div>
  )
}
