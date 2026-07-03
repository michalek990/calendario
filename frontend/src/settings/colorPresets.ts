export interface ColorPreset {
  id: string
  label: string
  primary: string
  primaryLight: string
  primaryLighter: string
}

export const COLOR_PRESETS: ColorPreset[] = [
  { id: 'blue', label: 'Jasny niebieski', primary: '#2f6fed', primaryLight: '#2559c7', primaryLighter: '#4a86ff' },
  { id: 'navy', label: 'Granat', primary: '#0f1f3d', primaryLight: '#1c3564', primaryLighter: '#274277' },
  { id: 'green', label: 'Zieleń', primary: '#1f8a52', primaryLight: '#166b3f', primaryLighter: '#28a866' },
  { id: 'purple', label: 'Fiolet', primary: '#6b46c1', primaryLight: '#55349b', primaryLighter: '#7e5cd6' },
  { id: 'teal', label: 'Turkusowy', primary: '#0f9488', primaryLight: '#0c766c', primaryLighter: '#14b3a3' },
]

export type FontSize = 'small' | 'medium' | 'large'

export const FONT_SIZE_PX: Record<FontSize, number> = {
  small: 14,
  medium: 16,
  large: 18,
}

export const FONT_SIZE_LABELS: Record<FontSize, string> = {
  small: 'Mała',
  medium: 'Średnia',
  large: 'Duża',
}
