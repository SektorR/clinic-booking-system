import React, { useState } from 'react'

interface DatePickerProps {
  label?: string
  value: string
  onChange: (date: string) => void
  min?: string
  required?: boolean
  className?: string
}

export const DatePicker: React.FC<DatePickerProps> = ({
  label,
  value,
  onChange,
  min,
  required,
  className = ''
}) => {
  const [isOpen, setIsOpen] = useState(false)
  const [currentMonth, setCurrentMonth] = useState(new Date())

  // Parse the current value or use today
  const selectedDate = value ? new Date(value + 'T00:00:00') : null

  // Removed click-outside handler since we have an explicit close button

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear()
    const month = date.getMonth()
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)
    const daysInMonth = lastDay.getDate()
    const startingDayOfWeek = firstDay.getDay()

    return { daysInMonth, startingDayOfWeek, year, month }
  }

  const formatDate = (date: Date | null) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const formatDisplayDate = (dateString: string) => {
    if (!dateString) return 'Select date'
    const date = new Date(dateString + 'T00:00:00')
    return date.toLocaleDateString('en-AU', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const handleDateSelect = (day: number) => {
    const year = currentMonth.getFullYear()
    const month = currentMonth.getMonth()
    const selected = new Date(year, month, day)
    onChange(formatDate(selected))
  }

  const isDateDisabled = (day: number) => {
    if (!min) return false
    const year = currentMonth.getFullYear()
    const month = currentMonth.getMonth()
    const date = new Date(year, month, day)
    const minDate = new Date(min + 'T00:00:00')
    return date < minDate
  }

  const isToday = (day: number) => {
    const today = new Date()
    const year = currentMonth.getFullYear()
    const month = currentMonth.getMonth()
    return (
      day === today.getDate() &&
      month === today.getMonth() &&
      year === today.getFullYear()
    )
  }

  const isSelected = (day: number) => {
    if (!selectedDate) return false
    const year = currentMonth.getFullYear()
    const month = currentMonth.getMonth()
    return (
      day === selectedDate.getDate() &&
      month === selectedDate.getMonth() &&
      year === selectedDate.getFullYear()
    )
  }

  const previousMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1))
  }

  const nextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1))
  }

  const { daysInMonth, startingDayOfWeek, year, month } = getDaysInMonth(currentMonth)
  const monthName = currentMonth.toLocaleDateString('en-AU', { month: 'long', year: 'numeric' })

  // Create array of day numbers
  const days = Array.from({ length: daysInMonth }, (_, i) => i + 1)
  const emptyDays = Array.from({ length: startingDayOfWeek }, (_, i) => i)

  return (
    <div className={`w-full ${className}`}>
      {label && (
        <label className="block text-sm font-medium text-stone-700 mb-1">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className="w-full px-3 py-2 bg-white text-stone-800 border-2 border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all duration-300 flex items-center justify-between hover:border-emerald-600"
        >
          <span className={value ? 'text-stone-800' : 'text-stone-400'}>
            {formatDisplayDate(value)}
          </span>
          <svg
            className="h-5 w-5 text-emerald-700"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
        </button>

        {isOpen && (
          <div className="mt-2 w-full bg-white border-2 border-emerald-700 rounded-lg shadow-lg p-4 transition-all duration-300">
            {/* Header with Month Navigation and Close Button */}
            <div className="flex items-center justify-between mb-4">
              <button
                type="button"
                onClick={previousMonth}
                className="p-2 hover:bg-stone-100 rounded-lg transition-all duration-300"
              >
                <svg className="h-5 w-5 text-stone-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <span className="font-semibold text-stone-800">{monthName}</span>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={nextMonth}
                  className="p-2 hover:bg-stone-100 rounded-lg transition-all duration-300"
                >
                  <svg className="h-5 w-5 text-stone-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="p-2 hover:bg-red-100 rounded-lg transition-all duration-300 text-stone-600 hover:text-red-700"
                  title="Close calendar"
                >
                  <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Days of Week */}
            <div className="grid grid-cols-7 gap-1 mb-2">
              {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((day) => (
                <div key={day} className="text-center text-xs font-semibold text-stone-600 py-1">
                  {day}
                </div>
              ))}
            </div>

            {/* Calendar Grid */}
            <div className="grid grid-cols-7 gap-2">
              {emptyDays.map((_, index) => (
                <div key={`empty-${index}`} className="h-8" />
              ))}
              {days.map((day) => {
                const disabled = isDateDisabled(day)
                const selected = isSelected(day)
                const today = isToday(day)

                return (
                  <button
                    key={day}
                    type="button"
                    onClick={() => !disabled && handleDateSelect(day)}
                    disabled={disabled}
                    className={`py-1.5 px-2 rounded-lg text-sm font-medium transition-all duration-300 ${
                      selected
                        ? 'bg-gradient-to-r from-emerald-50 to-amber-50 border-2 border-emerald-700 text-stone-800 shadow-md'
                        : today
                        ? 'border-2 border-amber-400 text-stone-800 hover:bg-amber-50'
                        : disabled
                        ? 'text-stone-300 cursor-not-allowed border-2 border-transparent'
                        : 'text-stone-700 hover:bg-stone-100 border-2 border-transparent hover:border-stone-200'
                    }`}
                  >
                    {day}
                  </button>
                )
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
