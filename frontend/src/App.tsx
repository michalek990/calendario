import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { DashboardPage } from './pages/DashboardPage'
import { LeaveRequestsPage } from './pages/LeaveRequestsPage'
import { TeamManagementPage } from './pages/TeamManagementPage'
import { TimeTrackingPage } from './pages/TimeTrackingPage'
import { ProjectsPage } from './pages/ProjectsPage'
import { ProfilePage } from './pages/ProfilePage'
import { NotificationsPage } from './pages/NotificationsPage'
import { AdminUsersPage } from './pages/AdminUsersPage'
import { AdminUserEditPage } from './pages/AdminUserEditPage'
import { AdminFacilitiesPage } from './pages/AdminFacilitiesPage'
import { SettingsPage } from './pages/SettingsPage'
import { ProtectedRoute } from './components/ProtectedRoute'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/leave-requests"
        element={
          <ProtectedRoute>
            <LeaveRequestsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/team"
        element={
          <ProtectedRoute allowedRoles={['MANAGER', 'HR', 'ADMIN']}>
            <TeamManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/time-tracking"
        element={
          <ProtectedRoute>
            <TimeTrackingPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/projects"
        element={
          <ProtectedRoute>
            <ProjectsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <NotificationsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <SettingsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminUsersPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminUserEditPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/facilities"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminFacilitiesPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

export default App
