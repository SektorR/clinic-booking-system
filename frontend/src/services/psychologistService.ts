import { api } from './api'
import {
  PsychologistDTO,
  UpdateProfileRequest,
  DashboardDTO,
  AppointmentDTO,
  AppointmentParams,
  UpdateStatusRequest,
  NotesRequest,
  AvailabilityDTO,
  AvailabilityRequest,
  TimeOffDTO,
  TimeOffRequest,
  ClientSummaryDTO,
  MessageDTO,
  MessageRequest
} from '../types'
import {
  mockPsychologists,
  mockSessionTypes,
  generateAvailableSlots,
  getMockBookings
} from '../data/mockData'

export const psychologistService = {
  // Public endpoints (no auth required) - Using mock data for demo
  getAllPsychologists: async (): Promise<PsychologistDTO[]> => {
    await new Promise(resolve => setTimeout(resolve, 300))
    return mockPsychologists.filter(p => p.isActive) as any
  },

  getPsychologistById: async (id: string): Promise<PsychologistDTO> => {
    await new Promise(resolve => setTimeout(resolve, 200))
    const psychologist = mockPsychologists.find(p => p.id === id)
    if (!psychologist) {
      throw new Error('Psychologist not found')
    }
    return psychologist as any
  },

  getSessionTypes: async (): Promise<any[]> => {
    await new Promise(resolve => setTimeout(resolve, 200))
    return mockSessionTypes.filter(s => s.isActive)
  },

  getAvailableSlots: async (psychologistId: string, date: string, durationMinutes?: number): Promise<any> => {
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

    const availableSlots = availableTimes
      .filter(time => !bookedTimes.includes(time))
      .map(time => {
        const [hours, minutes] = time.split(':')
        const slotDate = new Date(date)
        slotDate.setHours(parseInt(hours), parseInt(minutes), 0, 0)
        return {
          startTime: slotDate.toISOString(),
          available: true
        }
      })

    return {
      date,
      availableSlots
    }
  },

  // Profile
  getProfile: async (): Promise<PsychologistDTO> => {
    const response = await api.get<PsychologistDTO>('/psychologist/profile')
    return response.data
  },

  updateProfile: async (data: UpdateProfileRequest): Promise<PsychologistDTO> => {
    const response = await api.put<PsychologistDTO>('/psychologist/profile', data)
    return response.data
  },

  // Dashboard
  getDashboard: async (): Promise<DashboardDTO> => {
    const response = await api.get<DashboardDTO>('/psychologist/dashboard')
    return response.data
  },

  // Appointments
  getAppointments: async (params?: AppointmentParams): Promise<AppointmentDTO[]> => {
    const response = await api.get<AppointmentDTO[]>('/psychologist/appointments', { params })
    return response.data
  },

  getAppointmentDetails: async (id: string): Promise<AppointmentDTO> => {
    const response = await api.get<AppointmentDTO>(`/psychologist/appointments/${id}`)
    return response.data
  },

  updateAppointmentStatus: async (id: string, status: string): Promise<AppointmentDTO> => {
    const response = await api.put<AppointmentDTO>(`/psychologist/appointments/${id}/status`, { status })
    return response.data
  },

  addNotes: async (id: string, notes: string): Promise<AppointmentDTO> => {
    const response = await api.post<AppointmentDTO>(`/psychologist/appointments/${id}/notes`, { notes })
    return response.data
  },

  // Availability
  getAvailability: async (): Promise<AvailabilityDTO[]> => {
    const response = await api.get<AvailabilityDTO[]>('/psychologist/availability')
    return response.data
  },

  addAvailability: async (data: AvailabilityRequest): Promise<AvailabilityDTO> => {
    const response = await api.post<AvailabilityDTO>('/psychologist/availability', data)
    return response.data
  },

  updateAvailability: async (id: string, data: AvailabilityRequest): Promise<AvailabilityDTO> => {
    const response = await api.put<AvailabilityDTO>(`/psychologist/availability/${id}`, data)
    return response.data
  },

  deleteAvailability: async (id: string): Promise<void> => {
    await api.delete(`/psychologist/availability/${id}`)
  },

  // Time Off
  getTimeOff: async (): Promise<TimeOffDTO[]> => {
    const response = await api.get<TimeOffDTO[]>('/psychologist/availability/time-off')
    return response.data
  },

  addTimeOff: async (data: TimeOffRequest): Promise<TimeOffDTO> => {
    const response = await api.post<TimeOffDTO>('/psychologist/availability/time-off', data)
    return response.data
  },

  deleteTimeOff: async (id: string): Promise<void> => {
    await api.delete(`/psychologist/availability/time-off/${id}`)
  },

  // Clients
  getClients: async (): Promise<ClientSummaryDTO[]> => {
    const response = await api.get<ClientSummaryDTO[]>('/psychologist/clients')
    return response.data
  },

  getClientAppointments: async (clientId: string): Promise<AppointmentDTO[]> => {
    const response = await api.get<AppointmentDTO[]>(`/psychologist/clients/${clientId}/appointments`)
    return response.data
  },

  getClientMessages: async (clientId: string): Promise<MessageDTO[]> => {
    const response = await api.get<MessageDTO[]>(`/psychologist/clients/${clientId}/messages`)
    return response.data
  }
}

export const messageService = {
  sendMessage: async (data: MessageRequest): Promise<MessageDTO> => {
    const response = await api.post<MessageDTO>('/messages', data)
    return response.data
  },

  getThreadMessages: async (threadId: string): Promise<MessageDTO[]> => {
    const response = await api.get<MessageDTO[]>(`/messages/thread/${threadId}`)
    return response.data
  },

  getUnreadMessages: async (): Promise<MessageDTO[]> => {
    const response = await api.get<MessageDTO[]>('/messages/unread')
    return response.data
  },

  markAsRead: async (id: string): Promise<MessageDTO> => {
    const response = await api.put<MessageDTO>(`/messages/${id}/read`)
    return response.data
  }
}
