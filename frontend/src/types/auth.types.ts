export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  type?: string
  psychologistId: string
  email: string
  firstName: string
  lastName: string
  role: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  phone: string
  specialization: string
  registrationNumber: string
  bio?: string
}

export interface User {
  id: string
  firstName?: string
  lastName?: string
  name?: string  // Full name (can be used instead of firstName/lastName)
  email: string
  phone?: string
  specialization?: string
  registrationNumber?: string
  bio?: string
  role?: 'PSYCHOLOGIST' | 'ADMIN'
}
