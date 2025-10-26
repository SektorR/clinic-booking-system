import {
  Psychologist,
  SessionType,
  GuestBookingRequest,
  GuestBooking,
  CheckoutSessionResponse,
  CancellationResponse,
  RescheduleRequest,
  PublicAvailabilityDTO,
  AvailabilitySlot
} from '../types'
import {
  mockPsychologists,
  mockSessionTypes,
  generateAvailableSlots,
  initializeMockData,
  getMockBookings,
  saveMockBookings,
  generateConfirmationToken
} from '../data/mockData'

// Initialize mock data on load
initializeMockData()

/**
 * Mock booking service for demonstration without backend
 * Uses localStorage for data persistence
 */
export const bookingService = {
  /**
   * Get all active psychologists
   */
  getPsychologists: async (): Promise<Psychologist[]> => {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 300))
    return mockPsychologists.filter(p => p.isActive)
  },

  /**
   * Get a specific psychologist by ID
   */
  getPsychologistById: async (id: string): Promise<Psychologist> => {
    await new Promise(resolve => setTimeout(resolve, 200))
    const psychologist = mockPsychologists.find(p => p.id === id)
    if (!psychologist) {
      throw new Error('Psychologist not found')
    }
    return psychologist
  },

  /**
   * Get available time slots for a psychologist on a specific date
   */
  getAvailability: async (psychologistId: string, date: string): Promise<PublicAvailabilityDTO> => {
    await new Promise(resolve => setTimeout(resolve, 200))

    const selectedDate = new Date(date)
    const availableTimes = generateAvailableSlots(selectedDate, psychologistId)

    // Get existing bookings for this psychologist on this date
    const bookings = getMockBookings()
    const dateBookings = bookings.filter(b =>
      b.psychologistId === psychologistId &&
      b.appointmentDateTime.startsWith(date) &&
      b.bookingStatus !== 'CANCELLED'
    )

    // Mark booked slots as unavailable
    const bookedTimes = dateBookings.map(b => {
      const time = new Date(b.appointmentDateTime)
      return `${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`
    })

    const slots: AvailabilitySlot[] = availableTimes.map(time => ({
      startTime: time,
      endTime: time, // We'll calculate end time based on session duration
      available: !bookedTimes.includes(time)
    }))

    return {
      date,
      slots
    }
  },

  /**
   * Get all available session types
   */
  getAllSessionTypes: async (): Promise<SessionType[]> => {
    await new Promise(resolve => setTimeout(resolve, 200))
    return mockSessionTypes.filter(s => s.isActive)
  },

  /**
   * Create a new booking
   * For demo purposes, we skip actual Stripe payment and create booking directly
   */
  createBooking: async (bookingData: GuestBookingRequest): Promise<CheckoutSessionResponse> => {
    await new Promise(resolve => setTimeout(resolve, 500))

    // Find session type to get price and duration
    const sessionType = mockSessionTypes.find(s => s.id === bookingData.sessionTypeId)
    if (!sessionType) {
      throw new Error('Session type not found')
    }

    // Generate confirmation token
    const token = generateConfirmationToken()

    // Create the booking
    const newBooking: GuestBooking = {
      id: `booking-${Date.now()}`,
      ...bookingData,
      durationMinutes: sessionType.durationMinutes,
      amount: sessionType.price,
      paymentStatus: 'COMPLETED', // Mock payment as completed
      bookingStatus: 'CONFIRMED', // Mock booking as confirmed
      confirmationToken: token,
      emailConfirmed: true,
      reminderSent: false,
      stripeCheckoutSessionId: `demo_session_${Date.now()}`,
      stripePaymentIntentId: `demo_pi_${Date.now()}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }

    // Save to localStorage
    const bookings = getMockBookings()
    bookings.push(newBooking)
    saveMockBookings(bookings)

    // Return mock checkout URL (will redirect to success page immediately)
    return {
      checkoutUrl: `/booking/success?token=${token}`,
      sessionId: newBooking.stripeCheckoutSessionId!
    }
  },

  /**
   * Get booking details by confirmation token
   */
  getBookingByToken: async (token: string): Promise<GuestBooking> => {
    await new Promise(resolve => setTimeout(resolve, 200))

    const bookings = getMockBookings()
    const booking = bookings.find(b => b.confirmationToken === token)

    if (!booking) {
      throw new Error('Booking not found')
    }

    return booking
  },

  /**
   * Cancel a booking
   */
  cancelBooking: async (token: string): Promise<CancellationResponse> => {
    await new Promise(resolve => setTimeout(resolve, 300))

    const bookings = getMockBookings()
    const bookingIndex = bookings.findIndex(b => b.confirmationToken === token)

    if (bookingIndex === -1) {
      throw new Error('Booking not found')
    }

    const booking = bookings[bookingIndex]

    if (booking.bookingStatus === 'CANCELLED') {
      throw new Error('Booking is already cancelled')
    }

    // Check if within 24 hours (for refund policy)
    const appointmentTime = new Date(booking.appointmentDateTime).getTime()
    const now = Date.now()
    const hoursUntilAppointment = (appointmentTime - now) / (1000 * 60 * 60)

    const refundAmount = hoursUntilAppointment >= 24 ? booking.amount : 0

    // Update booking status
    booking.bookingStatus = 'CANCELLED'
    booking.paymentStatus = refundAmount > 0 ? 'REFUNDED' : booking.paymentStatus
    booking.updatedAt = new Date().toISOString()

    bookings[bookingIndex] = booking
    saveMockBookings(bookings)

    return {
      success: true,
      message: hoursUntilAppointment >= 24
        ? 'Booking cancelled successfully. Full refund has been processed.'
        : 'Booking cancelled. No refund available (less than 24 hours notice).',
      refundAmount: refundAmount > 0 ? refundAmount : undefined
    }
  },

  /**
   * Reschedule a booking
   */
  rescheduleBooking: async (token: string, request: RescheduleRequest): Promise<GuestBooking> => {
    await new Promise(resolve => setTimeout(resolve, 300))

    const bookings = getMockBookings()
    const bookingIndex = bookings.findIndex(b => b.confirmationToken === token)

    if (bookingIndex === -1) {
      throw new Error('Booking not found')
    }

    const booking = bookings[bookingIndex]

    if (booking.bookingStatus === 'CANCELLED') {
      throw new Error('Cannot reschedule a cancelled booking')
    }

    // Update appointment date/time
    booking.appointmentDateTime = request.newAppointmentDateTime
    booking.updatedAt = new Date().toISOString()

    bookings[bookingIndex] = booking
    saveMockBookings(bookings)

    return booking
  }
}
