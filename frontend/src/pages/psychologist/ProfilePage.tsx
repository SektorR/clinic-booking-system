import { useState, useEffect } from 'react'
import { psychologistService } from '../../services'
import { PsychologistDTO } from '../../types/psychologist.types'
import { Card, LoadingSpinner, Input, Button } from '../../components/common'

const ProfilePage = () => {
  const [profile, setProfile] = useState<PsychologistDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    specialization: '',
    bio: '',
  })

  useEffect(() => {
    loadProfile()
  }, [])

  const loadProfile = async () => {
    try {
      setLoading(true)
      const data = await psychologistService.getProfile()
      setProfile(data)
      setFormData({
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        specialization: data.specialization,
        bio: data.bio || '',
      })
    } catch (error) {
      console.error('Failed to load profile:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSave = async () => {
    try {
      setLoading(true)
      await psychologistService.updateProfile(formData)
      setEditing(false)
      loadProfile()
    } catch (error) {
      console.error('Failed to update profile:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading && !profile) return <LoadingSpinner />

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-stone-800">Profile</h1>
        {!editing && (
          <Button onClick={() => setEditing(true)}>Edit Profile</Button>
        )}
      </div>

      <Card>
        {editing ? (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="First Name"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              />
              <Input
                label="Last Name"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              />
            </div>
            <Input
              label="Phone"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
            <Input
              label="Specialization"
              value={formData.specialization}
              onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
            />
            <div>
              <label className="block text-sm font-medium text-stone-700 mb-2">Bio</label>
              <textarea
                className="w-full px-3 py-2 border border-stone-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all duration-300"
                rows={4}
                value={formData.bio}
                onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
              />
            </div>
            <div className="flex gap-2">
              <Button onClick={handleSave} loading={loading}>
                Save Changes
              </Button>
              <Button variant="secondary" onClick={() => setEditing(false)}>
                Cancel
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <div>
              <p className="text-sm text-stone-600">Name</p>
              <p className="font-semibold text-stone-800">{profile?.firstName} {profile?.lastName}</p>
            </div>
            <div>
              <p className="text-sm text-stone-600">Email</p>
              <p className="font-semibold text-stone-800">{profile?.email}</p>
            </div>
            <div>
              <p className="text-sm text-stone-600">Phone</p>
              <p className="font-semibold text-stone-800">{profile?.phone}</p>
            </div>
            <div>
              <p className="text-sm text-stone-600">Specialization</p>
              <p className="font-semibold text-stone-800">{profile?.specialization}</p>
            </div>
            <div>
              <p className="text-sm text-stone-600">Bio</p>
              <p className="text-stone-700">{profile?.bio || 'No bio set'}</p>
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}

export default ProfilePage
