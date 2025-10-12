import { api } from './api'
import { LoginRequest, LoginResponse, User } from '../types'

export const authService = {
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', { email, password })
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token)
    }
    return response.data
  },

  logout: (): void => {
    localStorage.removeItem('authToken')
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<User>('/psychologist/profile')
    return response.data
  },

  refreshToken: async (): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/refresh')
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token)
    }
    return response.data
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('authToken')
  }
}
