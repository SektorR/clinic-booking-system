import { Navigate, Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { RootState } from '../store/store'

/**
 * Protected route component that requires authentication
 * Redirects to login if not authenticated
 */
const ProtectedRoute = () => {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated)

  return isAuthenticated ? <Outlet /> : <Navigate to="/psychologist/login" replace />
}

export default ProtectedRoute
