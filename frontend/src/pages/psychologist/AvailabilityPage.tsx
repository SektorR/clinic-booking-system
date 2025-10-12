import { useState, useEffect } from 'react'
import { psychologistService } from '../../services'
import { AvailabilityDTO, TimeOffDTO, AvailabilityRequest } from '../../types/psychologist.types'
import { Card, LoadingSpinner, Button, Input, Modal } from '../../components/common'

const AvailabilityPage = () => {
  const [availability, setAvailability] = useState<AvailabilityDTO[]>([])
  const [timeOff, setTimeOff] = useState<TimeOffDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [showAvailabilityModal, setShowAvailabilityModal] = useState(false)
  const [showTimeOffModal, setShowTimeOffModal] = useState(false)

  const [newAvailability, setNewAvailability] = useState<AvailabilityRequest>({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '17:00',
    isRecurring: true,
  })

  const [newTimeOff, setNewTimeOff] = useState({
    startDateTime: '',
    endDateTime: '',
    reason: '',
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [avail, time] = await Promise.all([
        psychologistService.getAvailability(),
        psychologistService.getTimeOff(),
      ])
      setAvailability(avail)
      setTimeOff(time)
    } catch (error) {
      console.error('Failed to load data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAddAvailability = async () => {
    try {
      await psychologistService.addAvailability(newAvailability)
      setShowAvailabilityModal(false)
      loadData()
    } catch (error) {
      console.error('Failed to add availability:', error)
    }
  }

  const handleAddTimeOff = async () => {
    try {
      await psychologistService.addTimeOff(newTimeOff)
      setShowTimeOffModal(false)
      loadData()
    } catch (error) {
      console.error('Failed to add time off:', error)
    }
  }

  const handleDeleteAvailability = async (id: string) => {
    if (confirm('Delete this availability slot?')) {
      try {
        await psychologistService.deleteAvailability(id)
        loadData()
      } catch (error) {
        console.error('Failed to delete:', error)
      }
    }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-stone-800">Availability Management</h1>
      </div>

      {/* Weekly Schedule */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-stone-800">Weekly Schedule</h2>
          <Button onClick={() => setShowAvailabilityModal(true)}>Add Availability</Button>
        </div>

        <div className="space-y-3">
          {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].map(day => {
            const daySlots = availability.filter(a => a.dayOfWeek === day)
            return (
              <div key={day} className="border-b border-stone-200 pb-3">
                <p className="font-semibold text-stone-700 mb-2">{day}</p>
                {daySlots.length === 0 ? (
                  <p className="text-sm text-stone-500">No availability set</p>
                ) : (
                  <div className="space-y-2">
                    {daySlots.map(slot => (
                      <div key={slot.id} className="flex items-center justify-between bg-gradient-to-r from-emerald-50 to-amber-50 p-2 rounded border border-stone-200">
                        <span className="text-sm text-stone-700">{slot.startTime} - {slot.endTime}</span>
                        <Button
                          onClick={() => handleDeleteAvailability(slot.id)}
                          size="sm"
                          variant="danger"
                        >
                          Delete
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      </Card>

      {/* Time Off */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-stone-800">Time Off</h2>
          <Button onClick={() => setShowTimeOffModal(true)}>Schedule Time Off</Button>
        </div>

        {timeOff.length === 0 ? (
          <p className="text-stone-500">No time off scheduled</p>
        ) : (
          <div className="space-y-3">
            {timeOff.map(to => (
              <div key={to.id} className="border border-stone-200 p-4 rounded hover:border-amber-300 transition-all duration-300">
                <div className="flex justify-between">
                  <div>
                    <p className="font-semibold text-stone-800">
                      {new Date(to.startDateTime).toLocaleDateString()} - {new Date(to.endDateTime).toLocaleDateString()}
                    </p>
                    {to.reason && <p className="text-sm text-stone-600 mt-1">{to.reason}</p>}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Add Availability Modal */}
      <Modal isOpen={showAvailabilityModal} onClose={() => setShowAvailabilityModal(false)} title="Add Availability">
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-stone-700 mb-2">Day of Week</label>
            <select
              className="w-full px-3 py-2 border border-stone-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all duration-300"
              value={newAvailability.dayOfWeek}
              onChange={(e) => setNewAvailability({ ...newAvailability, dayOfWeek: e.target.value })}
            >
              {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].map(day => (
                <option key={day} value={day}>{day}</option>
              ))}
            </select>
          </div>
          <Input
            type="time"
            label="Start Time"
            value={newAvailability.startTime}
            onChange={(e) => setNewAvailability({ ...newAvailability, startTime: e.target.value })}
          />
          <Input
            type="time"
            label="End Time"
            value={newAvailability.endTime}
            onChange={(e) => setNewAvailability({ ...newAvailability, endTime: e.target.value })}
          />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setShowAvailabilityModal(false)}>Cancel</Button>
            <Button onClick={handleAddAvailability}>Add</Button>
          </div>
        </div>
      </Modal>

      {/* Add Time Off Modal */}
      <Modal isOpen={showTimeOffModal} onClose={() => setShowTimeOffModal(false)} title="Schedule Time Off">
        <div className="space-y-4">
          <Input
            type="datetime-local"
            label="Start"
            value={newTimeOff.startDateTime}
            onChange={(e) => setNewTimeOff({ ...newTimeOff, startDateTime: e.target.value })}
          />
          <Input
            type="datetime-local"
            label="End"
            value={newTimeOff.endDateTime}
            onChange={(e) => setNewTimeOff({ ...newTimeOff, endDateTime: e.target.value })}
          />
          <Input
            label="Reason (optional)"
            value={newTimeOff.reason}
            onChange={(e) => setNewTimeOff({ ...newTimeOff, reason: e.target.value })}
          />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setShowTimeOffModal(false)}>Cancel</Button>
            <Button onClick={handleAddTimeOff}>Schedule</Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default AvailabilityPage
