import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { Card, Button } from '../../components/common'

/**
 * Success page shown after successful Stripe payment
 */
const BookingSuccessPage = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const sessionId = searchParams.get('session_id')
  const [confirmationSent, setConfirmationSent] = useState(false)

  useEffect(() => {
    // Simulate confirmation email sent
    if (sessionId || token) {
      setTimeout(() => setConfirmationSent(true), 1000)
    }
  }, [sessionId, token])

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/40 via-stone-50/30 to-emerald-50/30 flex items-center justify-center py-12 px-4">
      <Card className="max-w-2xl">
        <div className="text-center">
          {/* Success Icon */}
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-emerald-100 mb-6">
            <svg
              className="h-10 w-10 text-emerald-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>

          <h1 className="text-3xl font-bold text-stone-800 mb-4">Booking Confirmed!</h1>

          <p className="text-lg text-stone-600 mb-8">
            Your appointment has been successfully booked and paid for.
          </p>

          {/* Confirmation Details */}
          <div className="bg-gradient-to-br from-stone-50 to-amber-50/30 rounded-lg p-6 mb-8 text-left border border-stone-200">
            <h2 className="text-lg font-semibold mb-4 text-stone-800">What's Next?</h2>

            <ul className="space-y-3 text-stone-700">
              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-emerald-700 mr-3 flex-shrink-0"
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
                <div>
                  <p className="font-medium text-stone-800">Confirmation Email Sent</p>
                  <p className="text-sm text-stone-600">
                    Check your email for appointment details and a management link
                  </p>
                </div>
              </li>

              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-emerald-700 mr-3 flex-shrink-0"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                  />
                </svg>
                <div>
                  <p className="font-medium text-stone-800">SMS Confirmation</p>
                  <p className="text-sm text-stone-600">
                    You'll also receive an SMS with appointment details
                  </p>
                </div>
              </li>

              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-emerald-700 mr-3 flex-shrink-0"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                  />
                </svg>
                <div>
                  <p className="font-medium text-stone-800">24-Hour Reminder</p>
                  <p className="text-sm text-stone-600">
                    We'll send you a reminder 24 hours before your appointment
                  </p>
                </div>
              </li>

              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-emerald-700 mr-3 flex-shrink-0"
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
                <div>
                  <p className="font-medium text-stone-800">Manage Your Booking</p>
                  <p className="text-sm text-stone-600">
                    Use the link in your email to cancel or reschedule (24 hours notice required)
                  </p>
                </div>
              </li>
            </ul>
          </div>

          {/* Payment Receipt */}
          <div className="bg-emerald-50 border border-emerald-200 rounded-lg p-4 mb-6">
            <p className="text-sm text-emerald-900">
              💳 A payment receipt has been emailed to you
            </p>
          </div>

          {/* Booking Management Link - Demo Feature */}
          {token && (
            <div className="bg-amber-50 border border-amber-300 rounded-lg p-4 mb-8">
              <p className="text-sm font-semibold text-amber-900 mb-2">
                📋 Manage Your Booking (Demo Feature)
              </p>
              <p className="text-xs text-amber-800 mb-3">
                In a real system, this link would be emailed to you. For demo purposes, you can access it here:
              </p>
              <Link to={`/booking/manage/${token}`}>
                <Button size="sm" variant="secondary">
                  View/Manage Booking
                </Button>
              </Link>
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/">
              <Button variant="secondary">Return Home</Button>
            </Link>
            <Link to="/psychologists">
              <Button>Book Another Appointment</Button>
            </Link>
          </div>
        </div>
      </Card>
    </div>
  )
}

export default BookingSuccessPage
