import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { psychologistService } from '../../services'
import { DashboardDTO } from '../../types/psychologist.types'
import { Card, LoadingSpinner } from '../../components/common'

/**
 * Dashboard page for psychologist portal
 * Shows today's appointments, upcoming appointments, and statistics
 */
const DashboardPage = () => {
  const [dashboard, setDashboard] = useState<DashboardDTO | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      setLoading(true)
      const data = await psychologistService.getDashboard()
      setDashboard(data)
    } catch (error) {
      console.error('Failed to load dashboard:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <LoadingSpinner />
  }

  if (!dashboard) {
    return <div>Failed to load dashboard</div>
  }

  const { todayAppointments, upcomingAppointments, stats } = dashboard

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div>
        <h1 className="text-3xl font-bold text-stone-800">Welcome Back</h1>
        <p className="text-stone-600 mt-1">Here's what's happening with your appointments today</p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="bg-gradient-to-br from-emerald-600 to-emerald-700 text-amber-50 border-emerald-700">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-emerald-100 text-sm">Total Sessions</p>
              <p className="text-3xl font-bold mt-1">{stats.totalSessions}</p>
            </div>
            <div className="bg-emerald-500 bg-opacity-30 p-3 rounded-full">
              <svg
                className="w-8 h-8"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
          </div>
        </Card>

        <Card className="bg-gradient-to-br from-amber-600 to-amber-700 text-amber-50 border-amber-700">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-amber-100 text-sm">Pending Bookings</p>
              <p className="text-3xl font-bold mt-1">{stats.pendingBookings}</p>
            </div>
            <div className="bg-amber-500 bg-opacity-30 p-3 rounded-full">
              <svg
                className="w-8 h-8"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
        </Card>

        <Card className="bg-gradient-to-br from-emerald-700 to-emerald-800 text-amber-50 border-emerald-800">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-emerald-100 text-sm">Completed This Week</p>
              <p className="text-3xl font-bold mt-1">{stats.completedThisWeek}</p>
            </div>
            <div className="bg-emerald-600 bg-opacity-30 p-3 rounded-full">
              <svg
                className="w-8 h-8"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
        </Card>

        <Card className="bg-gradient-to-br from-stone-600 to-stone-700 text-amber-50 border-stone-700">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-stone-100 text-sm">Unread Messages</p>
              <p className="text-3xl font-bold mt-1">{stats.unreadMessages || 0}</p>
            </div>
            <div className="bg-stone-500 bg-opacity-30 p-3 rounded-full">
              <svg
                className="w-8 h-8"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                />
              </svg>
            </div>
          </div>
        </Card>
      </div>

      {/* Today's Appointments */}
      <Card>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-stone-800">Today's Appointments</h2>
          <Link
            to="/psychologist/appointments"
            className="text-emerald-700 hover:text-emerald-800 text-sm font-medium transition-colors duration-300"
          >
            View All →
          </Link>
        </div>

        {todayAppointments.length === 0 ? (
          <div className="text-center py-12 text-stone-500">
            <svg
              className="mx-auto h-12 w-12 text-stone-400 mb-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <p>No appointments scheduled for today</p>
          </div>
        ) : (
          <div className="space-y-4">
            {todayAppointments.map((appointment) => (
              <div
                key={appointment.id}
                className="border border-stone-200 rounded-lg p-4 hover:border-amber-300 hover:bg-amber-50/30 transition-all duration-300"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="bg-gradient-to-br from-emerald-100 to-amber-100 text-emerald-700 rounded-full w-12 h-12 flex items-center justify-center font-semibold border-2 border-emerald-200">
                      {appointment.patientName.charAt(0)}
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg text-stone-800">{appointment.patientName}</h3>
                      <p className="text-stone-600 text-sm">{appointment.modality}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-stone-800">
                      {new Date(appointment.appointmentDateTime).toLocaleTimeString('en-AU', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </p>
                    <p className="text-sm text-stone-600">{appointment.durationMinutes} min</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Upcoming Appointments */}
      <Card>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-stone-800">Upcoming (Next 7 Days)</h2>
          <Link
            to="/psychologist/appointments"
            className="text-emerald-700 hover:text-emerald-800 text-sm font-medium transition-colors duration-300"
          >
            View All →
          </Link>
        </div>

        {upcomingAppointments.length === 0 ? (
          <div className="text-center py-12 text-stone-500">
            <p>No upcoming appointments in the next 7 days</p>
          </div>
        ) : (
          <div className="space-y-3">
            {upcomingAppointments.map((appointment) => (
              <div
                key={appointment.id}
                className="flex items-center justify-between p-3 border border-stone-200 rounded-lg hover:bg-amber-50/30 hover:border-amber-300 transition-all duration-300"
              >
                <div>
                  <p className="font-semibold text-stone-800">{appointment.patientName}</p>
                  <p className="text-sm text-stone-600">
                    {new Date(appointment.appointmentDateTime).toLocaleDateString('en-AU', {
                      weekday: 'short',
                      month: 'short',
                      day: 'numeric',
                    })}{' '}
                    at{' '}
                    {new Date(appointment.appointmentDateTime).toLocaleTimeString('en-AU', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
                <span className="text-sm text-stone-500">{appointment.modality}</span>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link to="/psychologist/appointments">
          <Card className="hover:shadow-lg hover:border-emerald-300 transition-all duration-300 cursor-pointer">
            <div className="text-center py-4">
              <svg
                className="mx-auto h-8 w-8 text-emerald-700 mb-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <p className="font-semibold text-stone-800">View All Appointments</p>
            </div>
          </Card>
        </Link>

        <Link to="/psychologist/availability">
          <Card className="hover:shadow-lg hover:border-emerald-300 transition-all duration-300 cursor-pointer">
            <div className="text-center py-4">
              <svg
                className="mx-auto h-8 w-8 text-emerald-700 mb-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <p className="font-semibold text-stone-800">Manage Availability</p>
            </div>
          </Card>
        </Link>

        <Link to="/psychologist/messages">
          <Card className="hover:shadow-lg hover:border-emerald-300 transition-all duration-300 cursor-pointer">
            <div className="text-center py-4">
              <svg
                className="mx-auto h-8 w-8 text-emerald-700 mb-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
                />
              </svg>
              <p className="font-semibold text-stone-800">Messages</p>
            </div>
          </Card>
        </Link>
      </div>
    </div>
  )
}

export default DashboardPage
