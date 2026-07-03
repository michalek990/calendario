import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import type { LoginPayload, RegisterPayload } from '../api/types'
import { decodeJwtPayload, isJwtExpired, type Role } from './jwt'

const TOKEN_STORAGE_KEY = 'calendario.token'

interface AuthContextValue {
  token: string | null
  email: string | null
  role: Role | null
  isAuthenticated: boolean
  hasAnyRole: (...roles: Role[]) => boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredToken(): string | null {
  const stored = localStorage.getItem(TOKEN_STORAGE_KEY)
  if (stored && !isJwtExpired(stored)) {
    return stored
  }
  if (stored) {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
  }
  return null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => readStoredToken())

  const setSession = (newToken: string) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, newToken)
    setToken(newToken)
  }

  const login = async (payload: LoginPayload) => {
    const response = await authApi.login(payload)
    setSession(response.token)
  }

  const register = async (payload: RegisterPayload) => {
    const response = await authApi.register(payload)
    setSession(response.token)
  }

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken(null)
  }

  const payload = useMemo(() => (token ? decodeJwtPayload(token) : null), [token])
  const email = payload?.sub ?? null
  const role = payload?.role ?? null

  const hasAnyRole = (...roles: Role[]) => role !== null && roles.includes(role)

  const value: AuthContextValue = {
    token,
    email,
    role,
    isAuthenticated: token !== null,
    hasAnyRole,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth musi być użyty wewnątrz AuthProvider')
  }
  return context
}
