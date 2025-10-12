import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, LoadingSpinner } from '../../components/common'
import { bookingService } from '../../services'
import { Psychologist } from '../../types'

export const PsychologistListPage: React.FC = () => {
  const navigate = useNavigate()
  const [psychologists, setPsychologists] = useState<Psychologist[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState('')

  useEffect(() => {
    loadPsychologists()
  }, [])

  const loadPsychologists = async () => {
    try {
      setLoading(true)
      const data = await bookingService.getPsychologists()
      setPsychologists(data)
      setError(null)
    } catch (err) {
      setError('Failed to load psychologists. Please try again.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const filteredPsychologists = psychologists.filter(
    (p) =>
      p.firstName.toLowerCase().includes(filter.toLowerCase()) ||
      p.lastName.toLowerCase().includes(filter.toLowerCase()) ||
      p.specialization.toLowerCase().includes(filter.toLowerCase())
  )

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 via-stone-50 to-emerald-50">

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-4xl font-bold mb-8 text-stone-800">Find a Psychologist</h1>

        {/* Search/Filter */}
        <div className="mb-8">
          <input
            type="text"
            placeholder="Search by name or specialization..."
            className="w-full px-4 py-3 border border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all duration-300"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex justify-center py-12">
            <LoadingSpinner size="lg" text="Loading psychologists..." />
          </div>
        )}

        {/* Error State */}
        {error && (
          <Card className="bg-red-50 border border-red-200">
            <p className="text-red-600">{error}</p>
            <Button onClick={loadPsychologists} className="mt-4">
              Try Again
            </Button>
          </Card>
        )}

        {/* Psychologists Grid */}
        {!loading && !error && (
          <>
            {filteredPsychologists.length === 0 ? (
              <Card>
                <p className="text-stone-600 text-center">
                  No psychologists found matching your search.
                </p>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredPsychologists.map((psychologist) => (
                  <Card key={psychologist.id} hover>
                    <div className="mb-4 text-center">
                      <div className="inline-block p-1 rounded-full bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900 shadow-md mb-3">
                        <div className="bg-amber-50 w-20 h-20 rounded-full flex items-center justify-center">
                          <span className="text-2xl font-bold text-stone-800">
                            {psychologist.firstName[0]}
                            {psychologist.lastName[0]}
                          </span>
                        </div>
                      </div>
                      <h3 className="text-xl font-semibold text-center text-stone-800">
                        {psychologist.firstName} {psychologist.lastName}
                      </h3>
                      <p className="text-emerald-700 text-sm text-center font-medium">
                        {psychologist.specialization}
                      </p>
                    </div>

                    {psychologist.bio && (
                      <p className="text-stone-600 text-sm mb-4 line-clamp-3">
                        {psychologist.bio}
                      </p>
                    )}

                    <Button
                      fullWidth
                      onClick={() => navigate(`/book/${psychologist.id}`)}
                    >
                      Book Appointment
                    </Button>
                  </Card>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

export default PsychologistListPage
