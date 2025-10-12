import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from 'react-router-dom'
import { Provider, useDispatch } from 'react-redux'
import { store } from './store/store'
import ProtectedRoute from './components/ProtectedRoute'
import { Navbar, Footer } from './components/common'

// Patient Portal Pages
import HomePage from './pages/patient/HomePage'
import PsychologistListPage from './pages/patient/PsychologistListPage'
import BookingPage from './pages/patient/BookingPage'
import BookingSuccessPage from './pages/patient/BookingSuccessPage'
import ManageBookingPage from './pages/patient/ManageBookingPage'

// Psychologist Portal Pages
import LoginPage from './pages/psychologist/LoginPage'
import DashboardPage from './pages/psychologist/DashboardPage'
import AppointmentsPage from './pages/psychologist/AppointmentsPage'
import AvailabilityPage from './pages/psychologist/AvailabilityPage'
import MessagesPage from './pages/psychologist/MessagesPage'
import ProfilePage from './pages/psychologist/ProfilePage'

/**
 * Main App component with routing for both patient and psychologist portals
 */
function App() {
  return (
    <Provider store={store}>
      <Router>
        <div className="min-h-screen bg-gray-50 flex flex-col">
          <Routes>
            {/* Patient Portal - Public Routes */}
            <Route
              path="/"
              element={
                <>
                  <Navbar type="patient" />
                  <main className="flex-grow">
                    <HomePage />
                  </main>
                  <Footer />
                </>
              }
            />
            <Route
              path="/psychologists"
              element={
                <>
                  <Navbar type="patient" />
                  <main className="flex-grow">
                    <PsychologistListPage />
                  </main>
                  <Footer />
                </>
              }
            />
            <Route
              path="/book/:psychologistId"
              element={
                <>
                  <Navbar type="patient" />
                  <main className="flex-grow">
                    <BookingPage />
                  </main>
                  <Footer />
                </>
              }
            />
            <Route
              path="/booking/success"
              element={
                <>
                  <Navbar type="patient" />
                  <main className="flex-grow">
                    <BookingSuccessPage />
                  </main>
                  <Footer />
                </>
              }
            />
            <Route
              path="/booking/manage/:token"
              element={
                <>
                  <Navbar type="patient" />
                  <main className="flex-grow">
                    <ManageBookingPage />
                  </main>
                  <Footer />
                </>
              }
            />

            {/* Psychologist Portal - Login (Public) */}
            <Route path="/portal" element={<LoginPage />} />
            <Route path="/portal/login" element={<LoginPage />} />
            <Route path="/psychologist/login" element={<LoginPage />} />

            {/* Psychologist Portal - Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route
                path="/psychologist/dashboard"
                element={
                  <div className="flex h-screen">
                    <PsychologistSidebar />
                    <main className="flex-grow overflow-auto p-8">
                      <DashboardPage />
                    </main>
                  </div>
                }
              />
              <Route
                path="/psychologist/appointments"
                element={
                  <div className="flex h-screen">
                    <PsychologistSidebar />
                    <main className="flex-grow overflow-auto p-8">
                      <AppointmentsPage />
                    </main>
                  </div>
                }
              />
              <Route
                path="/psychologist/availability"
                element={
                  <div className="flex h-screen">
                    <PsychologistSidebar />
                    <main className="flex-grow overflow-auto p-8">
                      <AvailabilityPage />
                    </main>
                  </div>
                }
              />
              <Route
                path="/psychologist/messages"
                element={
                  <div className="flex h-screen">
                    <PsychologistSidebar />
                    <main className="flex-grow overflow-auto p-8">
                      <MessagesPage />
                    </main>
                  </div>
                }
              />
              <Route
                path="/psychologist/profile"
                element={
                  <div className="flex h-screen">
                    <PsychologistSidebar />
                    <main className="flex-grow overflow-auto p-8">
                      <ProfilePage />
                    </main>
                  </div>
                }
              />
            </Route>

            {/* 404 Page */}
            <Route
              path="*"
              element={
                <div className="min-h-screen flex items-center justify-center">
                  <div className="text-center">
                    <h1 className="text-4xl font-bold text-gray-900 mb-4">404</h1>
                    <p className="text-gray-600">Page not found</p>
                  </div>
                </div>
              }
            />
          </Routes>
        </div>
      </Router>
    </Provider>
  )
}

/**
 * Sidebar navigation for psychologist portal
 */
const PsychologistSidebar = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const dispatch = useDispatch()

  const handleLogout = () => {
    dispatch({ type: 'auth/logout' })
    navigate('/psychologist/login')
  }

  const isActive = (path: string) => location.pathname === path

  const navItems = [
    { path: '/psychologist/dashboard', label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { path: '/psychologist/appointments', label: 'Appointments', icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { path: '/psychologist/availability', label: 'Availability', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
    { path: '/psychologist/messages', label: 'Messages', icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z' },
    { path: '/psychologist/profile', label: 'Profile', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
  ]

  return (
    <aside className="w-64 bg-primary-900 text-white flex flex-col">
      <div className="p-6 border-b border-primary-800">
        <h1 className="text-xl font-bold">Ground & Grow</h1>
        <p className="text-primary-300 text-sm mt-1">Psychologist Portal</p>
      </div>

      <nav className="flex-grow p-4 space-y-2">
        {navItems.map((item) => (
          <button
            key={item.path}
            onClick={() => navigate(item.path)}
            className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg transition ${
              isActive(item.path)
                ? 'bg-primary-700 text-white'
                : 'text-primary-200 hover:bg-primary-800 hover:text-white'
            }`}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
            </svg>
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="p-4 border-t border-primary-800">
        <button
          onClick={handleLogout}
          className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-primary-200 hover:bg-primary-800 hover:text-white transition"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
            />
          </svg>
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}

export default App
