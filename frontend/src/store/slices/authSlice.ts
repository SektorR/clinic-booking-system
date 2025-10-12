import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { User } from '../../types'

interface AuthState {
  token: string | null
  user: User | null
  isAuthenticated: boolean
  loading: boolean
  error: string | null
}

const initialState: AuthState = {
  token: localStorage.getItem('authToken'),
  user: null,
  isAuthenticated: !!localStorage.getItem('authToken'),
  loading: false,
  error: null
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action: PayloadAction<{ token: string; user: User }>) => {
      state.token = action.payload.token
      state.user = action.payload.user
      state.isAuthenticated = true
      state.error = null
      // Store token in localStorage for API interceptor
      localStorage.setItem('authToken', action.payload.token)
    },
    setUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload
    },
    logout: (state) => {
      state.token = null
      state.user = null
      state.isAuthenticated = false
      state.error = null
      localStorage.removeItem('authToken')
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
    }
  }
})

export const { setCredentials, setUser, logout, setLoading, setError, clearError } = authSlice.actions
export default authSlice.reducer
