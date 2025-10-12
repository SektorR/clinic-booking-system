import { api } from './api'
import {
  Psychologist,
  SessionType,
  GuestBookingRequest,
  GuestBooking,
  CheckoutSessionResponse,
  CancellationResponse,
  RescheduleRequest,
  AvailabilityDTO
} from '../types'

export const bookingService = {
  getPsychologists: async (): Promise<Psychologist[]> => {
    const response = await api.get<Psychologist[]>('/public/psychologists')
    return response.data
  },

  getPsychologistById: async (id: string): Promise<Psychologist> => {
    const response = await api.get<Psychologist>(`/public/psychologists/${id}`)
    return response.data
  },

  getAvailability: async (psychologistId: string, date: string): Promise<AvailabilityDTO> => {
    const response = await api.get<AvailabilityDTO>(
      `/public/psychologists/${psychologistId}/availability`,
      { params: { date } }
    )
    return response.data
  },

  getAllSessionTypes: async (): Promise<SessionType[]> => {
    const response = await api.get<SessionType[]>('/public/psychologists/session-types')
    return response.data
  },

  createBooking: async (bookingData: GuestBookingRequest): Promise<CheckoutSessionResponse> => {
    const response = await api.post<CheckoutSessionResponse>('/public/bookings', bookingData)
    return response.data
  },

  getBookingByToken: async (token: string): Promise<GuestBooking> => {
    const response = await api.get<GuestBooking>(`/public/bookings/${token}`)
    return response.data
  },

  cancelBooking: async (token: string): Promise<CancellationResponse> => {
    const response = await api.put<CancellationResponse>(`/public/bookings/${token}/cancel`)
    return response.data
  },

  rescheduleBooking: async (token: string, request: RescheduleRequest): Promise<GuestBooking> => {
    const response = await api.put<GuestBooking>(`/public/bookings/${token}/reschedule`, request)
    return response.data
  }
}
