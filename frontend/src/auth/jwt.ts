export type Role = 'EMPLOYEE' | 'MANAGER' | 'HR_ADMIN'

interface JwtPayload {
  sub?: string
  role?: Role
  firstName?: string
  lastName?: string
  exp?: number
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  const parts = token.split('.')
  if (parts.length !== 3) {
    return null
  }
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64)) as JwtPayload
  } catch {
    return null
  }
}

export function isJwtExpired(token: string): boolean {
  const payload = decodeJwtPayload(token)
  if (!payload?.exp) {
    return true
  }
  return payload.exp * 1000 < Date.now()
}
