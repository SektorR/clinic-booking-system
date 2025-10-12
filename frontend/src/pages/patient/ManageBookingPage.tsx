import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { bookingService } from '../../services'
import { GuestBooking } from '../../types/booking.types'
import { Card, Button, LoadingSpinner, Modal, Input } from '../../components/common'

/**
 * Page for managing guest bookings via confirmation token
 * Accessed via email link - no login required
 */
const ManageBookingPage = () => {
  const { token } = useParams<{ token: string }>()
  const [loading, setLoading] = useState(true)
  const [booking, setBooking] = useState<GuestBooking | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [showCancelModal, setShowCancelModal] = useState(false)
  const [showRescheduleModal, setShowRescheduleModal] = useState(false)
  const [newDateTime, setNewDateTime] = useState('')

  useEffect(() => {
    if (token) {
      loadBooking()
    }
  }, [token])

  const loadBooking = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await bookingService.getBookingByToken(token!)
      setBooking(data)
    } catch (err) {
      setError('Unable to find booking. Please check your confirmation link.')
      console.error('Failed to load booking:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCancelBooking = async () => {
    try {
      setLoading(true)
      await bookingService.cancelBooking(token!)
      setShowCancelModal(false)
      loadBooking() // Reload to show updated status
      alert('Booking cancelled successfully. You will receive a refund if eligible.')
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel booking')
    } finally {
      setLoading(false)
    }
  }

  const handleReschedule = async () => {
    if (!newDateTime) {
      alert('Please select a new date and time')
      return
    }

    try {
      setLoading(true)
      await bookingService.rescheduleBooking(token!, { newAppointmentDateTime: newDateTime })
      setShowRescheduleModal(false)
      loadBooking() // Reload to show updated details
      alert('Booking rescheduled successfully!')
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reschedule booking')
    } finally {
      setLoading(false)
    }
  }

  if (loading && !booking) {
    return <LoadingSpinner />
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-amber-50/40 via-stone-50/30 to-emerald-50/30 flex items-center justify-center py-12 px-4">
        <Card className="max-w-md text-center">
          <div className="text-red-600 mb-4">
            <svg
              className="mx-auto h-12 w-12"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-stone-800 mb-2">Booking Not Found</h2>
          <p className="text-stone-600">{error}</p>
        </Card>
      </div>
    )
  }

  if (!booking) return null

  const canCancel = booking.bookingStatus === 'CONFIRMED'
  const canReschedule = booking.bookingStatus === 'CONFIRMED'
  const isCancelled = booking.bookingStatus === 'CANCELLED'
  const isCompleted = booking.bookingStatus === 'COMPLETED'

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/40 via-stone-50/30 to-emerald-50/30 py-12 px-4">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-3xl font-bold text-stone-800 mb-8">Manage Your Appointment</h1>

        {/* Booking Status Banner */}
        <div
          className={`mb-6 p-4 rounded-lg ${
            isCancelled
              ? 'bg-red-50 border border-red-200'
              : isCompleted
              ? 'bg-green-50 border border-green-200'
              : 'bg-blue-50 border border-blue-200'
          }`}
        >
          <p
            className={`font-semibold ${
              isCancelled
                ? 'text-red-900'
                : isCompleted
                ? 'text-green-900'
                : 'text-blue-900'
            }`}
          >
            Status: {booking.bookingStatus.replace('_', ' ').toUpperCase()}
          </p>
        </div>

        <Card>
          <h2 className="text-2xl font-bold mb-6">Appointment Details</h2>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-stone-600">Patient Name</p>
                <p className="font-semibold text-stone-800">
                  {booking.firstName} {booking.lastName}
                </p>
              </div>
              <div>
                <p className="text-sm text-stone-600">Email</p>
                <p className="font-semibold text-stone-800">{booking.email}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-stone-600">Phone</p>
                <p className="font-semibold text-stone-800">{booking.phone}</p>
              </div>
              <div>
                <p className="text-sm text-stone-600">Modality</p>
                <p className="font-semibold text-stone-800">{booking.modality}</p>
              </div>
            </div>

            <div>
              <p className="text-sm text-stone-600">Appointment Date & Time</p>
              <p className="font-semibold text-lg text-stone-800">
                {new Date(booking.appointmentDateTime).toLocaleString('en-AU', {
                  weekday: 'long',
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            </div>

            <div>
              <p className="text-sm text-stone-600">Duration</p>
              <p className="font-semibold text-stone-800">{booking.durationMinutes} minutes</p>
            </div>

            {booking.notes && (
              <div>
                <p className="text-sm text-stone-600">Your Notes</p>
                <p className="font-semibold text-stone-700">{booking.notes}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4 pt-4 border-t">
              <div>
                <p className="text-sm text-stone-600">Payment Status</p>
                <p className="font-semibold text-stone-800">{booking.paymentStatus}</p>
              </div>
              <div>
                <p className="text-sm text-stone-600">Amount Paid</p>
                <p className="font-semibold text-lg text-stone-800">${booking.amount.toFixed(2)} AUD</p>
              </div>
            </div>
          </div>

          {/* Actions */}
          {!isCancelled && !isCompleted && (
            <div className="mt-8 pt-6 border-t flex flex-col sm:flex-row gap-4">
              {canReschedule && (
                <Button onClick={() => setShowRescheduleModal(true)} variant="secondary">
                  Reschedule Appointment
                </Button>
              )}
              {canCancel && (
                <Button
                  onClick={() => setShowCancelModal(true)}
                  variant="danger"
                >
                  Cancel Appointment
                </Button>
              )}
            </div>
          )}

          {/* Cancellation Policy */}
          {canCancel && (
            <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <p className="text-sm text-yellow-900">
                <strong>Cancellation Policy:</strong> Cancel at least 24 hours before your
                appointment to receive a full refund. Late cancellations may not be eligible for a
                refund.
              </p>
            </div>
          )}
        </Card>

        {/* Cancel Modal */}
        <Modal
          isOpen={showCancelModal}
          onClose={() => setShowCancelModal(false)}
          title="Cancel Appointment"
        >
          <p className="text-stone-700 mb-6">
            Are you sure you want to cancel this appointment? If you cancel within the policy
            timeframe, you will receive a refund.
          </p>
          <div className="flex gap-4 justify-end">
            <Button variant="secondary" onClick={() => setShowCancelModal(false)}>
              Keep Appointment
            </Button>
            <Button
              onClick={handleCancelBooking}
              disabled={loading}
              variant="danger"
              loading={loading}
            >
              Yes, Cancel
            </Button>
          </div>
        </Modal>

        {/* Reschedule Modal */}
        <Modal
          isOpen={showRescheduleModal}
          onClose={() => setShowRescheduleModal(false)}
          title="Reschedule Appointment"
        >
          <div className="mb-6">
            <Input
              type="datetime-local"
              label="New Date & Time"
              value={newDateTime}
              onChange={(e) => setNewDateTime(e.target.value)}
              required
            />
          </div>
          <div className="flex gap-4 justify-end">
            <Button variant="secondary" onClick={() => setShowRescheduleModal(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleReschedule}
              disabled={!newDateTime}
              loading={loading}
            >
              Confirm Reschedule
            </Button>
          </div>
        </Modal>
      </div>
    </div>
  )
}

export default ManageBookingPage
