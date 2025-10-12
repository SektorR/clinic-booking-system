import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { bookingService, psychologistService } from '../../services'
import { GuestBookingRequest, SessionType } from '../../types/booking.types'
import { PsychologistDTO } from '../../types/psychologist.types'
import { Button, Input, Card, LoadingSpinner, DatePicker } from '../../components/common'
import { setBookingData } from '../../store/slices/bookingSlice'

/**
 * Multi-step booking page for creating guest appointments
 */
const BookingPage = () => {
  const { psychologistId } = useParams<{ psychologistId: string }>()
  const navigate = useNavigate()
  const dispatch = useDispatch()

  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [psychologist, setPsychologist] = useState<PsychologistDTO | null>(null)
  const [sessionTypes, setSessionTypes] = useState<SessionType[]>([])
  const [selectedDate, setSelectedDate] = useState<string>('')
  const [availableSlots, setAvailableSlots] = useState<any[]>([])
  const [loadingSlots, setLoadingSlots] = useState(false)

  const [formData, setFormData] = useState<Partial<GuestBookingRequest>>({
    psychologistId: psychologistId || '',
    modality: 'ONLINE',
  })

  useEffect(() => {
    if (psychologistId) {
      loadPsychologistData()
    }
  }, [psychologistId])

  // Auto-select first available date and time when reaching step 2
  useEffect(() => {
    if (step === 2 && !selectedDate && formData.sessionTypeId) {
      loadFirstAvailableSlot()
    }
  }, [step, formData.sessionTypeId])

  const loadFirstAvailableSlot = async () => {
    if (!psychologistId || !formData.sessionTypeId) return

    try {
      setLoadingSlots(true)
      const today = new Date().toISOString().split('T')[0]

      // Try to find the first available date within the next 30 days
      for (let i = 0; i < 30; i++) {
        const date = new Date()
        date.setDate(date.getDate() + i)
        const dateString = date.toISOString().split('T')[0]

        const selectedSessionType = sessionTypes.find(st => st.id === formData.sessionTypeId)
        const duration = selectedSessionType?.durationMinutes || 60

        const availability = await psychologistService.getAvailableSlots(
          psychologistId,
          dateString,
          duration
        )

        if (availability.availableSlots && availability.availableSlots.length > 0) {
          // Found available slots - set the date and time
          setSelectedDate(dateString)
          setAvailableSlots(availability.availableSlots)
          // Auto-select the first slot
          handleTimeSlotSelect(availability.availableSlots[0])
          break
        }
      }
    } catch (error) {
      console.error('Failed to load first available slot:', error)
    } finally {
      setLoadingSlots(false)
    }
  }

  const loadPsychologistData = async () => {
    try {
      setLoading(true)
      const psychData = await psychologistService.getPsychologistById(psychologistId!)
      setPsychologist(psychData)

      const sessionData = await psychologistService.getSessionTypes()
      setSessionTypes(sessionData)
    } catch (error) {
      console.error('Failed to load psychologist data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: keyof GuestBookingRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleDateChange = async (date: string) => {
    setSelectedDate(date)
    setAvailableSlots([])

    if (!psychologistId || !date) return

    try {
      setLoadingSlots(true)
      const selectedSessionType = sessionTypes.find(st => st.id === formData.sessionTypeId)
      const duration = selectedSessionType?.durationMinutes || 60

      const availability = await psychologistService.getAvailableSlots(
        psychologistId,
        date,
        duration
      )
      setAvailableSlots(availability.availableSlots || [])
    } catch (error) {
      console.error('Failed to load availability:', error)
    } finally {
      setLoadingSlots(false)
    }
  }

  const handleTimeSlotSelect = (slot: any) => {
    const dateTime = slot.startTime
    handleInputChange('appointmentDateTime', dateTime)
  }

  const handleNextStep = () => {
    setStep((prev) => prev + 1)
  }

  const handlePrevStep = () => {
    setStep((prev) => prev - 1)
  }

  const handleSubmit = async () => {
    try {
      setLoading(true)

      const booking = formData as GuestBookingRequest
      const response = await bookingService.createBooking(booking)

      // Save booking details to Redux
      dispatch(setBookingData(formData))

      // Redirect to Stripe checkout
      window.location.href = response.checkoutUrl
    } catch (error) {
      console.error('Failed to create booking:', error)
      alert('Failed to create booking. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (loading && !psychologist) {
    return <LoadingSpinner />
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/40 via-stone-50/30 to-emerald-50/30 py-12">
      <div className="max-w-3xl mx-auto px-4">
        {/* Progress Indicator */}
        <div className="mb-8 px-6">
          {/* Step circles and connecting lines */}
          <div className="flex items-center mb-3">
            {[1, 2, 3, 4].map((s, index) => (
              <div key={s} className="flex items-center flex-1">
                <div className="flex justify-center" style={{ width: '60px' }}>
                  {step >= s ? (
                    <div className="inline-block p-1 rounded-full bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900 shadow-md">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                        step === s ? 'bg-white' : 'bg-amber-50'
                      }`}>
                        <span className="text-lg font-bold text-stone-800">{s}</span>
                      </div>
                    </div>
                  ) : (
                    <div className="bg-stone-200 text-stone-500 w-10 h-10 rounded-full flex items-center justify-center transition-all duration-500">
                      {s}
                    </div>
                  )}
                </div>
                <div
                  className={`flex-1 h-1 mx-2 transition-all duration-500 ${
                    step > s ? 'bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900' : 'bg-stone-200'
                  }`}
                />
              </div>
            ))}
          </div>
          {/* Step labels aligned with circles */}
          <div className="flex items-center">
            {[
              { label: 'Session Type' },
              { label: 'Date & Time' },
              { label: 'Details' },
              { label: 'Review' }
            ].map((item, index) => (
              <div key={index} className="flex items-center flex-1">
                <div className="text-xs text-stone-600 text-center" style={{ width: '60px' }}>
                  {item.label}
                </div>
                {index < 3 && <div className="flex-1"></div>}
              </div>
            ))}
          </div>
        </div>

        <Card>
          {/* Step 1: Select Session Type */}
          {step === 1 && (
            <div className="w-full">
              <h2 className="text-2xl font-bold mb-4 text-stone-800">Select Session Type</h2>
              <p className="text-stone-600 mb-6">
                Booking with {psychologist?.firstName} {psychologist?.lastName}
              </p>

              <div className="space-y-4">
                {sessionTypes.map((sessionType) => (
                  <div
                    key={sessionType.id}
                    onClick={() => {
                      handleInputChange('sessionTypeId', sessionType.id)
                      handleInputChange('modality', sessionType.modality)
                    }}
                    className={`w-full p-4 border-2 rounded-lg cursor-pointer transition-all duration-300 ${
                      formData.sessionTypeId === sessionType.id
                        ? 'border-emerald-700 bg-gradient-to-r from-emerald-50 to-amber-50 shadow-md'
                        : 'border-stone-200 hover:border-amber-300 bg-white hover:shadow-sm'
                    }`}
                  >
                    <div className="flex justify-between items-center">
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg text-stone-800">{sessionType.name}</h3>
                        <p className="text-stone-600 mt-1">{sessionType.description}</p>
                        <p className="text-sm text-stone-500 mt-2">
                          {sessionType.durationMinutes} minutes • {sessionType.modality}
                        </p>
                      </div>
                      <div className="text-right ml-4 flex-shrink-0">
                        <p className="text-2xl font-bold text-stone-700">
                          ${sessionType.price}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-6 flex justify-end">
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handleNextStep}
                      disabled={!formData.sessionTypeId}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 2: Select Date and Time */}
          {step === 2 && (
            <div className="w-full">
              <h2 className="text-2xl font-bold mb-4 text-stone-800">Select Date & Time</h2>

              <div className="w-full space-y-6">
                {/* Date Picker */}
                <DatePicker
                  label="Select Date"
                  value={selectedDate}
                  onChange={handleDateChange}
                  min={new Date().toISOString().split('T')[0]}
                  required
                />

                {/* Time Slot Picker */}
                {selectedDate && (
                  <div>
                    <label className="block text-sm font-medium text-stone-700 mb-2">
                      Available Time Slots
                    </label>

                    {loadingSlots ? (
                      <div className="text-center py-8">
                        <LoadingSpinner />
                      </div>
                    ) : availableSlots.length === 0 ? (
                      <p className="text-stone-500 text-center py-8">
                        No available time slots for this date. Please select another date.
                      </p>
                    ) : (
                      <div className="grid grid-cols-3 gap-3">
                        {availableSlots.map((slot, index) => {
                          const startTime = new Date(slot.startTime)
                          const timeString = startTime.toLocaleTimeString('en-US', {
                            hour: 'numeric',
                            minute: '2-digit',
                            hour12: true
                          })
                          const isSelected = formData.appointmentDateTime === slot.startTime

                          return (
                            <button
                              key={index}
                              onClick={() => handleTimeSlotSelect(slot)}
                              className={`p-3 border-2 rounded-lg text-center transition-all duration-300 ${
                                isSelected
                                  ? 'border-emerald-700 bg-gradient-to-r from-emerald-50 to-amber-50 text-stone-800 font-semibold shadow-md'
                                  : 'border-stone-200 hover:border-amber-300 hover:bg-amber-50/30 text-stone-700'
                              }`}
                              type="button"
                            >
                              {timeString}
                            </button>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div className="mt-6 flex justify-between">
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-stone-400 via-stone-500 to-stone-400 rounded-lg blur-xl opacity-0 group-hover:opacity-60 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handlePrevStep}
                      variant="secondary"
                    >
                      Back
                    </Button>
                  </div>
                </div>
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handleNextStep}
                      disabled={!formData.appointmentDateTime}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Personal Information */}
          {step === 3 && (
            <div className="w-full">
              <h2 className="text-2xl font-bold mb-4 text-stone-800">Your Information</h2>

              <div className="w-full space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <Input
                    label="First Name"
                    value={formData.firstName || ''}
                    onChange={(e) => handleInputChange('firstName', e.target.value)}
                    required
                  />
                  <Input
                    label="Last Name"
                    value={formData.lastName || ''}
                    onChange={(e) => handleInputChange('lastName', e.target.value)}
                    required
                  />
                </div>

                <Input
                  type="email"
                  label="Email"
                  value={formData.email || ''}
                  onChange={(e) => handleInputChange('email', e.target.value)}
                  required
                />

                <Input
                  type="tel"
                  label="Phone"
                  value={formData.phone || ''}
                  onChange={(e) => handleInputChange('phone', e.target.value)}
                  required
                />

                <Input
                  type="date"
                  label="Date of Birth (optional)"
                  value={formData.dateOfBirth || ''}
                  onChange={(e) => handleInputChange('dateOfBirth', e.target.value)}
                />

                <div>
                  <label className="block text-sm font-medium text-stone-700 mb-2">
                    Notes (optional)
                  </label>
                  <textarea
                    className="w-full px-3 py-2 bg-white text-stone-800 border border-stone-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-emerald-600 transition-all duration-300 placeholder:text-stone-400"
                    rows={4}
                    value={formData.notes || ''}
                    onChange={(e) => handleInputChange('notes', e.target.value)}
                    placeholder="Any additional information or concerns..."
                  />
                </div>
              </div>

              <div className="mt-6 flex justify-between">
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-stone-400 via-stone-500 to-stone-400 rounded-lg blur-xl opacity-0 group-hover:opacity-60 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handlePrevStep}
                      variant="secondary"
                    >
                      Back
                    </Button>
                  </div>
                </div>
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handleNextStep}
                      disabled={!formData.firstName || !formData.lastName || !formData.email || !formData.phone}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Review and Submit */}
          {step === 4 && (
            <div className="w-full">
              <h2 className="text-2xl font-bold mb-4 text-stone-800">Review & Confirm</h2>

              <div className="bg-gradient-to-br from-stone-50 to-amber-50/30 p-6 rounded-lg space-y-4 border border-stone-200">
                <div>
                  <p className="text-sm text-stone-600">Psychologist</p>
                  <p className="font-semibold text-stone-800">
                    {psychologist?.firstName} {psychologist?.lastName}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-stone-600">Appointment</p>
                  <p className="font-semibold text-stone-800">
                    {new Date(formData.appointmentDateTime!).toLocaleString()}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-stone-600">Your Details</p>
                  <p className="font-semibold text-stone-800">
                    {formData.firstName} {formData.lastName}
                  </p>
                  <p className="text-stone-700">{formData.email}</p>
                  <p className="text-stone-700">{formData.phone}</p>
                </div>

                {formData.notes && (
                  <div>
                    <p className="text-sm text-stone-600">Notes</p>
                    <p className="text-stone-700">{formData.notes}</p>
                  </div>
                )}
              </div>

              <div className="mt-6 flex justify-between">
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-stone-400 via-stone-500 to-stone-400 rounded-lg blur-xl opacity-0 group-hover:opacity-60 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handlePrevStep}
                      variant="secondary"
                    >
                      Back
                    </Button>
                  </div>
                </div>
                <div className="relative group">
                  <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
                  <div className="relative">
                    <Button
                      onClick={handleSubmit}
                      loading={loading}
                    >
                      Proceed to Payment
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </Card>
      </div>
    </div>
  )
}

export default BookingPage
