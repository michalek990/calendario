import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { changePassword } from '../api/auth'
import { ApiError } from '../api/types'
import { useSettings } from '../settings/SettingsContext'
import { COLOR_PRESETS, FONT_SIZE_LABELS, type FontSize } from '../settings/colorPresets'

export function SettingsPage() {
  const { token } = useAuth()
  const { theme, setTheme, fontSize, setFontSize, colorPresetId, setColorPresetId } = useSettings()

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [passwordError, setPasswordError] = useState<string | null>(null)
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChangePassword = async (event: FormEvent) => {
    event.preventDefault()
    setPasswordError(null)
    setPasswordSuccess(null)

    if (newPassword !== confirmPassword) {
      setPasswordError('Nowe hasło i jego powtórzenie nie są takie same')
      return
    }

    if (!token) return

    setIsSubmitting(true)
    try {
      await changePassword({ currentPassword, newPassword }, token)
      setPasswordSuccess('Hasło zostało zmienione')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err) {
      setPasswordError(err instanceof ApiError ? err.message : 'Nie udało się zmienić hasła')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Ustawienia</h1>

      <section className="settings-section">
        <h2>Wygląd</h2>

        <div className="settings-row">
          <span>Tryb ciemny</span>
          <label className="switch">
            <input
              type="checkbox"
              checked={theme === 'dark'}
              onChange={(e) => setTheme(e.target.checked ? 'dark' : 'light')}
            />
            <span className="switch-track" />
          </label>
        </div>

        <div className="settings-row">
          <span>Rozmiar czcionki</span>
          <select value={fontSize} onChange={(e) => setFontSize(e.target.value as FontSize)}>
            {Object.entries(FONT_SIZE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="settings-row settings-row-column">
          <span>Kolor wiodący</span>
          <div className="color-swatches">
            {COLOR_PRESETS.map((preset) => (
              <button
                key={preset.id}
                type="button"
                className={`color-swatch ${preset.id === colorPresetId ? 'selected' : ''}`}
                style={{ background: preset.primary }}
                onClick={() => setColorPresetId(preset.id)}
                aria-label={preset.label}
                title={preset.label}
              />
            ))}
          </div>
        </div>
      </section>

      <section className="settings-section">
        <h2>Zmiana hasła</h2>

        <form className="settings-form" onSubmit={handleChangePassword}>
          <label htmlFor="currentPassword">Obecne hasło</label>
          <input
            id="currentPassword"
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            required
          />

          <label htmlFor="newPassword">Nowe hasło</label>
          <input
            id="newPassword"
            type="password"
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
          />

          <label htmlFor="confirmPassword">Powtórz nowe hasło</label>
          <input
            id="confirmPassword"
            type="password"
            minLength={8}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />

          {passwordError && <p className="auth-error">{passwordError}</p>}
          {passwordSuccess && <p className="settings-success">{passwordSuccess}</p>}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Zapisywanie…' : 'Zmień hasło'}
          </button>
        </form>
      </section>
    </div>
  )
}
