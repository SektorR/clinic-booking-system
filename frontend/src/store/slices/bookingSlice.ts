import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { Psychologist, SessionType, GuestBookingRequest } from '../../types'

interface BookingState {
  selectedPsychologist: Psychologist | null
  selectedSessionType: SessionType | null
  selectedDate: string | null
  selectedTime: string | null
  bookingData: Partial<GuestBookingRequest>
  currentStep: number
  loading: boolean
  error: string | null
}

const initialState: BookingState = {
  selectedPsychologist: null,
  selectedSessionType: null,
  selectedDate: null,
  selectedTime: null,
  bookingData: {},
  currentStep: 1,
  loading: false,
  error: null
}

const bookingSlice = createSlice({
  name: 'booking',
  initialState,
  reducers: {
    setPsychologist: (state, action: PayloadAction<Psychologist>) => {
      state.selectedPsychologist = action.payload
      state.bookingData.psychologistId = action.payload.id
    },
    setSessionType: (state, action: PayloadAction<SessionType>) => {
      state.selectedSessionType = action.payload
      state.bookingData.sessionTypeId = action.payload.id
      state.bookingData.modality = action.payload.modality
    },
    setDateTime: (state, action: PayloadAction<{ date: string; time: string }>) => {
      state.selectedDate = action.payload.date
      state.selectedTime = action.payload.time
      state.bookingData.appointmentDateTime = `${action.payload.date}T${action.payload.time}`
    },
    setBookingData: (state, action: PayloadAction<Partial<GuestBookingRequest>>) => {
      state.bookingData = { ...state.bookingData, ...action.payload }
    },
    setStep: (state, action: PayloadAction<number>) => {
      state.currentStep = action.payload
    },
    nextStep: (state) => {
      state.currentStep += 1
    },
    previousStep: (state) => {
      state.currentStep -= 1
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload
      state.loading = false
    },
    clearError: (state) => {
      state.error = null
    },
    resetBooking: (state) => {
      return initialState
    }
  }
})

export const {
  setPsychologist,
  setSessionType,
  setDateTime,
  setBookingData,
  setStep,
  nextStep,
  previousStep,
  setLoading,
  setError,
  clearError,
  resetBooking
} = bookingSlice.actions

export default bookingSlice.reducer
