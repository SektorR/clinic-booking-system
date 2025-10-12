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

export const psychologistService = {
  // Public endpoints (no auth required)
  getAllPsychologists: async (): Promise<PsychologistDTO[]> => {
    const response = await api.get<PsychologistDTO[]>('/public/psychologists')
    return response.data
  },

  getPsychologistById: async (id: string): Promise<PsychologistDTO> => {
    const response = await api.get<PsychologistDTO>(`/public/psychologists/${id}`)
    return response.data
  },

  getSessionTypes: async (): Promise<any[]> => {
    const response = await api.get<any[]>('/public/psychologists/session-types')
    return response.data
  },

  getAvailableSlots: async (psychologistId: string, date: string, durationMinutes?: number): Promise<any> => {
    const params = durationMinutes ? { date, durationMinutes } : { date }
    const response = await api.get<any>(`/public/psychologists/${psychologistId}/availability`, { params })
    return response.data
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
