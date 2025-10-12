import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { authService } from '../../services'
import { setCredentials } from '../../store/slices/authSlice'
import { Card, Input, Button } from '../../components/common'

/**
 * Login page for psychologists
 */
const LoginPage = () => {
  const navigate = useNavigate()
  const dispatch = useDispatch()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    try {
      setLoading(true)
      const response = await authService.login(email, password)

      // Store token and user data in Redux
      dispatch(
        setCredentials({
          token: response.token,
          user: {
            id: response.psychologistId,
            email: response.email,
            firstName: response.firstName,
            lastName: response.lastName,
            name: `${response.firstName} ${response.lastName}`,
            role: response.role as 'PSYCHOLOGIST' | 'ADMIN',
          },
        })
      )

      // Redirect to dashboard
      navigate('/psychologist/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid email or password')
      console.error('Login failed:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/40 via-stone-50/30 to-emerald-50/30 flex items-center justify-center py-12 px-4">
      <Card className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-stone-800 mb-2">Psychologist Portal</h1>
          <p className="text-stone-600">Sign in to access your dashboard</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}

          <Input
            type="email"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
            placeholder="your.email@example.com"
          />

          <Input
            type="password"
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
            placeholder="Enter your password"
          />

          <Button type="submit" fullWidth loading={loading}>
            Sign In
          </Button>
        </form>

        <div className="mt-6 text-center text-sm text-stone-600">
          <p>Need help? Contact your administrator</p>
        </div>
      </Card>
    </div>
  )
}

export default LoginPage
