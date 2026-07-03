import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import type { Role } from '../auth/jwt'
import { AppLayout } from './AppLayout'

export function ProtectedRoute({ children, allowedRoles }: { children: ReactNode; allowedRoles?: Role[] }) {
  const { isAuthenticated, hasAnyRole } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !hasAnyRole(...allowedRoles)) {
    return <Navigate to="/dashboard" replace />
  }

  return <AppLayout>{children}</AppLayout>
}
