import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { COLOR_PRESETS, FONT_SIZE_PX, type FontSize } from './colorPresets'

const SETTINGS_STORAGE_KEY = 'calendario.settings'

export type Theme = 'light' | 'dark'

interface StoredSettings {
  theme: Theme
  fontSize: FontSize
  colorPresetId: string
}

const DEFAULT_SETTINGS: StoredSettings = {
  theme: 'light',
  fontSize: 'medium',
  colorPresetId: 'blue',
}

interface SettingsContextValue extends StoredSettings {
  setTheme: (theme: Theme) => void
  setFontSize: (size: FontSize) => void
  setColorPresetId: (id: string) => void
}

const SettingsContext = createContext<SettingsContextValue | undefined>(undefined)

function readStoredSettings(): StoredSettings {
  try {
    const raw = localStorage.getItem(SETTINGS_STORAGE_KEY)
    if (!raw) return DEFAULT_SETTINGS
    return { ...DEFAULT_SETTINGS, ...(JSON.parse(raw) as Partial<StoredSettings>) }
  } catch {
    return DEFAULT_SETTINGS
  }
}

export function SettingsProvider({ children }: { children: ReactNode }) {
  const [settings, setSettings] = useState<StoredSettings>(() => readStoredSettings())

  useEffect(() => {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify(settings))

    const root = document.documentElement
    root.dataset.theme = settings.theme
    root.style.fontSize = `${FONT_SIZE_PX[settings.fontSize]}px`

    const preset = COLOR_PRESETS.find((p) => p.id === settings.colorPresetId) ?? COLOR_PRESETS[0]
    root.style.setProperty('--primary', preset.primary)
    root.style.setProperty('--primary-light', preset.primaryLight)
    root.style.setProperty('--primary-lighter', preset.primaryLighter)
  }, [settings])

  const value: SettingsContextValue = {
    ...settings,
    setTheme: (theme) => setSettings((current) => ({ ...current, theme })),
    setFontSize: (fontSize) => setSettings((current) => ({ ...current, fontSize })),
    setColorPresetId: (colorPresetId) => setSettings((current) => ({ ...current, colorPresetId })),
  }

  return <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>
}

export function useSettings(): SettingsContextValue {
  const context = useContext(SettingsContext)
  if (!context) {
    throw new Error('useSettings musi być użyty wewnątrz SettingsProvider')
  }
  return context
}
