export interface PsychologistDTO {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string
  specialization: string
  registrationNumber: string
  bio?: string
  isActive: boolean
}

export interface UpdateProfileRequest {
  firstName?: string
  lastName?: string
  phone?: string
  specialization?: string
  bio?: string
}

export interface DashboardDTO {
  psychologist: PsychologistDTO
  todayAppointments: AppointmentDTO[]
  upcomingAppointments: AppointmentDTO[]
  stats: DashboardStats
}

export interface DashboardStats {
  totalSessions: number
  pendingBookings: number
  completedThisWeek: number
  completedThisMonth: number
  cancelledThisMonth: number
  noShowsThisMonth: number
  unreadMessages: number
}

export interface AppointmentDTO {
  id: string
  firstName: string
  lastName: string
  patientName: string  // Computed from firstName + lastName
  email: string
  phone: string
  psychologistId: string
  psychologistName?: string
  sessionTypeId: string
  sessionTypeName?: string
  appointmentDateTime: string
  durationMinutes: number
  modality: string
  amount: number
  paymentStatus: string
  bookingStatus: string
  notes?: string
  psychologistNotes?: string
  cancellationReason?: string
  meetingLink?: string
  roomNumber?: string
  reminderSent?: boolean
  createdAt: string
  updatedAt?: string
}

export interface AppointmentParams {
  startDate?: string
  endDate?: string
  status?: string
}

export interface UpdateStatusRequest {
  status: 'COMPLETED' | 'NO_SHOW'
}

export interface NotesRequest {
  notes: string
}

export interface AvailabilityRequest {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
  startTime: string
  endTime: string
  isRecurring: boolean
  effectiveFrom?: string
  effectiveUntil?: string
}

export interface AvailabilityDTO {
  id: string
  psychologistId: string
  dayOfWeek: string
  startTime: string
  endTime: string
  isRecurring: boolean
  effectiveFrom?: string
  effectiveUntil?: string
}

export interface TimeOffRequest {
  startDateTime: string
  endDateTime: string
  reason?: string
}

export interface TimeOffDTO {
  id: string
  psychologistId: string
  startDateTime: string
  endDateTime: string
  reason?: string
  createdAt: string
}

export interface ClientSummaryDTO {
  id: string
  name: string
  email: string
  phone: string
  totalAppointments: number
  lastAppointment: string
}

export interface MessageDTO {
  id: string
  senderId: string
  receiverId: string
  senderType: 'CLIENT' | 'PSYCHOLOGIST' | 'SYSTEM'
  receiverType: 'CLIENT' | 'PSYCHOLOGIST'
  subject: string
  content: string
  appointmentId?: string
  threadId: string
  isRead: boolean
  readAt?: string
  createdAt: string
}

export interface MessageRequest {
  receiverId: string
  receiverType: 'CLIENT' | 'PSYCHOLOGIST'
  subject: string
  content: string
  appointmentId?: string
  threadId?: string
}
