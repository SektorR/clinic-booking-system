import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { RootState } from '../../store/store'
import { logout } from '../../store/slices/authSlice'
import { Button } from './Button'

interface NavbarProps {
  type: 'patient' | 'psychologist'
}

export const Navbar: React.FC<NavbarProps> = ({ type }) => {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/psychologist/login')
  }

  if (type === 'patient') {
    return (
      <nav className="bg-gradient-to-r from-stone-50 via-amber-50/30 to-emerald-50/40 shadow-md border-b border-stone-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link to="/" className="flex items-center">
                <div className="flex items-end gap-2.5 h-16">
                  <span className="text-2xl font-serif text-emerald-800 pb-2">Ground</span>
                  <span className="text-3xl font-serif text-emerald-700 pb-2">&</span>
                  <span className="text-4xl font-serif text-emerald-700 leading-none pb-2">Grow</span>
                  <span className="text-base italic text-stone-500 pb-2">Psychology</span>
                </div>
              </Link>
            </div>
            <div className="flex items-center space-x-4">
              <Link to="/" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
                Home
              </Link>
              <Link to="/psychologists" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
                Find a Psychologist
              </Link>
            </div>
          </div>
        </div>
      </nav>
    )
  }

  return (
    <nav className="bg-gradient-to-r from-stone-50 via-amber-50/30 to-emerald-50/40 shadow-md border-b border-stone-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/psychologist/dashboard" className="flex items-center">
              <div className="flex items-end gap-2.5 h-16">
                <span className="text-2xl font-serif text-emerald-800 pb-2">Ground</span>
                <span className="text-3xl font-serif text-emerald-700 pb-2">&</span>
                <span className="text-4xl font-serif text-emerald-700 leading-none pb-2">Grow</span>
                <span className="text-base italic text-stone-500 pb-2">Portal</span>
              </div>
            </Link>
          </div>
          <div className="flex items-center space-x-4">
            <Link to="/psychologist/dashboard" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
              Dashboard
            </Link>
            <Link to="/psychologist/appointments" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
              Appointments
            </Link>
            <Link to="/psychologist/availability" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
              Availability
            </Link>
            <Link to="/psychologist/messages" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
              Messages
            </Link>
            <Link to="/psychologist/profile" className="text-stone-700 hover:text-emerald-700 transition-colors duration-300">
              Profile
            </Link>
            {isAuthenticated && (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-stone-700">
                  {user?.firstName} {user?.lastName}
                </span>
                <Button variant="secondary" size="sm" onClick={handleLogout}>
                  Logout
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
