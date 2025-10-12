import { useState, useEffect } from 'react'
import { psychologistService } from '../../services'
import { AppointmentDTO } from '../../types/psychologist.types'
import { Card, LoadingSpinner, Input, Button } from '../../components/common'

const AppointmentsPage = () => {
  const [appointments, setAppointments] = useState<AppointmentDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ startDate: '', endDate: '', status: '' })

  useEffect(() => {
    loadAppointments()
  }, [])

  const loadAppointments = async () => {
    try {
      setLoading(true)
      const data = await psychologistService.getAppointments(filters)
      setAppointments(data)
    } catch (error) {
      console.error('Failed to load appointments:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleFilter = () => {
    loadAppointments()
  }

  const handleStatusUpdate = async (id: string, status: string) => {
    try {
      await psychologistService.updateAppointmentStatus(id, status)
      loadAppointments()
    } catch (error) {
      console.error('Failed to update status:', error)
    }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-stone-800">Appointments</h1>

      {/* Filters */}
      <Card>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Input
            type="date"
            label="Start Date"
            value={filters.startDate}
            onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
          />
          <Input
            type="date"
            label="End Date"
            value={filters.endDate}
            onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
          />
          <div>
            <label className="block text-sm font-medium text-stone-700 mb-2">Status</label>
            <select
              className="w-full px-3 py-2 border border-stone-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all duration-300"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <option value="">All</option>
              <option value="confirmed">Confirmed</option>
              <option value="completed">Completed</option>
              <option value="cancelled">Cancelled</option>
              <option value="no_show">No Show</option>
            </select>
          </div>
          <div className="flex items-end">
            <Button onClick={handleFilter}>Apply Filters</Button>
          </div>
        </div>
      </Card>

      {/* Appointments List */}
      <div className="space-y-4">
        {appointments.length === 0 ? (
          <Card>
            <p className="text-center text-stone-500 py-8">No appointments found</p>
          </Card>
        ) : (
          appointments.map((apt) => (
            <Card key={apt.id}>
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-stone-800">{apt.patientName}</h3>
                  <p className="text-stone-600">{apt.email}</p>
                  <p className="text-sm text-stone-500 mt-1">
                    {new Date(apt.appointmentDateTime).toLocaleString()}
                  </p>
                </div>
                <div className="text-right">
                  <span className={`px-3 py-1 rounded-full text-sm ${
                    apt.bookingStatus.toLowerCase() === 'completed' ? 'bg-emerald-100 text-emerald-800' :
                    apt.bookingStatus.toLowerCase() === 'confirmed' ? 'bg-amber-100 text-amber-800' :
                    'bg-stone-100 text-stone-800'
                  }`}>
                    {apt.bookingStatus}
                  </span>
                  {apt.bookingStatus.toLowerCase() === 'confirmed' && (
                    <div className="mt-2 flex gap-2">
                      <Button
                        onClick={() => handleStatusUpdate(apt.id, 'completed')}
                        size="sm"
                      >
                        Mark Complete
                      </Button>
                      <Button
                        onClick={() => handleStatusUpdate(apt.id, 'no_show')}
                        size="sm"
                        variant="danger"
                      >
                        No Show
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  )
}

export default AppointmentsPage
