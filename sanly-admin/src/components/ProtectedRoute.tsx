import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export default function ProtectedRoute({ children }: { children?: React.ReactNode }) {
  const { token, roles } = useAuthStore()
  if (!token) return <Navigate to="/login" replace />
  if (!roles.includes('ROLE_ADMIN')) return <Navigate to="/login" replace />
  return children ? <>{children}</> : <Outlet />
}
