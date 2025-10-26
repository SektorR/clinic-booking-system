import { Psychologist, SessionType, GuestBooking } from '../types'

/**
 * Mock psychologists data for demonstration
 */
export const mockPsychologists: Psychologist[] = [
  {
    id: 'psy-001',
    firstName: 'Dr. Sarah',
    lastName: 'Mitchell',
    email: 'sarah.mitchell@groundandgrow.com',
    phone: '+61 2 8765 4321',
    specialization: 'Clinical Psychology, Anxiety & Depression',
    registrationNumber: 'PSY0012345',
    bio: 'Dr. Sarah Mitchell is a clinical psychologist with over 15 years of experience specializing in anxiety disorders, depression, and cognitive behavioral therapy (CBT). She adopts a compassionate, evidence-based approach to help clients achieve lasting positive change.',
    isActive: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 'psy-002',
    firstName: 'Dr. James',
    lastName: 'Chen',
    email: 'james.chen@groundandgrow.com',
    phone: '+61 2 8765 4322',
    specialization: 'Trauma & PTSD, Family Therapy',
    registrationNumber: 'PSY0023456',
    bio: 'Dr. James Chen specializes in trauma-focused therapy and family counseling. With a background in EMDR and narrative therapy, he helps individuals and families heal from traumatic experiences and strengthen their relationships.',
    isActive: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 'psy-003',
    firstName: 'Dr. Emma',
    lastName: 'Rodriguez',
    email: 'emma.rodriguez@groundandgrow.com',
    phone: '+61 2 8765 4323',
    specialization: 'Child & Adolescent Psychology, ADHD',
    registrationNumber: 'PSY0034567',
    bio: 'Dr. Emma Rodriguez is passionate about supporting children and adolescents through developmental challenges. She specializes in ADHD, autism spectrum disorders, and behavioral interventions using play therapy and parent coaching.',
    isActive: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 'psy-004',
    firstName: 'Dr. Michael',
    lastName: 'Thompson',
    email: 'michael.thompson@groundandgrow.com',
    phone: '+61 2 8765 4324',
    specialization: 'Addiction & Recovery, Mindfulness',
    registrationNumber: 'PSY0045678',
    bio: 'Dr. Michael Thompson combines traditional therapeutic approaches with mindfulness-based interventions to support clients in addiction recovery. His holistic approach addresses both the psychological and lifestyle factors in recovery.',
    isActive: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 'psy-005',
    firstName: 'Dr. Lisa',
    lastName: 'Nguyen',
    email: 'lisa.nguyen@groundandgrow.com',
    phone: '+61 2 8765 4325',
    specialization: 'Relationship Counseling, LGBTQ+ Support',
    registrationNumber: 'PSY0056789',
    bio: 'Dr. Lisa Nguyen is an affirming therapist who specializes in relationship counseling and LGBTQ+ mental health. She creates a safe, non-judgmental space for individuals and couples to explore their concerns and strengthen connections.',
    isActive: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  }
]

/**
 * Mock session types/services offered
 */
export const mockSessionTypes: SessionType[] = [
  {
    id: 'session-001',
    name: 'Initial Consultation (60 min)',
    description: 'First appointment to discuss your needs and treatment plan',
    durationMinutes: 60,
    price: 150.00,
    modality: 'ONLINE',
    isActive: true
  },
  {
    id: 'session-002',
    name: 'Standard Session (50 min)',
    description: 'Regular therapeutic session',
    durationMinutes: 50,
    price: 120.00,
    modality: 'ONLINE',
    isActive: true
  },
  {
    id: 'session-003',
    name: 'Extended Session (90 min)',
    description: 'Extended session for in-depth work',
    durationMinutes: 90,
    price: 200.00,
    modality: 'ONLINE',
    isActive: true
  },
  {
    id: 'session-004',
    name: 'In-Person Session (50 min)',
    description: 'Face-to-face therapy at our clinic',
    durationMinutes: 50,
    price: 130.00,
    modality: 'IN_PERSON',
    isActive: true
  },
  {
    id: 'session-005',
    name: 'Phone Consultation (30 min)',
    description: 'Brief phone check-in or follow-up',
    durationMinutes: 30,
    price: 80.00,
    modality: 'PHONE',
    isActive: true
  }
]

/**
 * Generate available time slots for a given date and psychologist
 */
export const generateAvailableSlots = (date: Date, psychologistId: string): string[] => {
  const dayOfWeek = date.getDay()

  // Weekend - limited availability
  if (dayOfWeek === 0 || dayOfWeek === 6) {
    return ['09:00', '10:00', '11:00', '13:00', '14:00']
  }

  // Weekday - full availability
  return [
    '09:00', '10:00', '11:00', '12:00',
    '13:00', '14:00', '15:00', '16:00', '17:00'
  ]
}

/**
 * Initialize localStorage with mock data if not already present
 */
export const initializeMockData = (): void => {
  if (!localStorage.getItem('mockBookings')) {
    localStorage.setItem('mockBookings', JSON.stringify([]))
  }
}

/**
 * Get all bookings from localStorage
 */
export const getMockBookings = (): GuestBooking[] => {
  const bookings = localStorage.getItem('mockBookings')
  return bookings ? JSON.parse(bookings) : []
}

/**
 * Save bookings to localStorage
 */
export const saveMockBookings = (bookings: GuestBooking[]): void => {
  localStorage.setItem('mockBookings', JSON.stringify(bookings))
}

/**
 * Generate a random confirmation token
 */
export const generateConfirmationToken = (): string => {
  return `demo-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`
}
