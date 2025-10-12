export interface Psychologist {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string
  specialization: string
  registrationNumber: string
  bio: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface SessionType {
  id: string
  name: string
  description: string
  durationMinutes: number
  price: number
  modality: 'ONLINE' | 'IN_PERSON' | 'PHONE'
  isActive: boolean
}

export interface GuestBookingRequest {
  firstName: string
  lastName: string
  email: string
  phone: string
  dateOfBirth?: string
  psychologistId: string
  sessionTypeId: string
  appointmentDateTime: string
  modality: string
  notes?: string
}

export interface GuestBooking {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string
  dateOfBirth?: string
  psychologistId: string
  sessionTypeId: string
  appointmentDateTime: string
  durationMinutes: number
  modality: string
  notes?: string
  stripePaymentIntentId?: string
  stripeCheckoutSessionId?: string
  amount: number
  paymentStatus: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'
  bookingStatus: 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'
  confirmationToken: string
  emailConfirmed: boolean
  reminderSent: boolean
  createdAt: string
  updatedAt: string
}

export interface CheckoutSessionResponse {
  checkoutUrl: string
  sessionId: string
}

export interface CancellationResponse {
  success: boolean
  message: string
  refundAmount?: number
}

export interface RescheduleRequest {
  newAppointmentDateTime: string
}

export interface AvailabilitySlot {
  startTime: string
  endTime: string
  available: boolean
}

export interface PublicAvailabilityDTO {
  date: string
  slots: AvailabilitySlot[]
}
