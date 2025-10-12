import React from 'react'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline'
  size?: 'sm' | 'md' | 'lg'
  fullWidth?: boolean
  loading?: boolean
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  loading = false,
  className = '',
  disabled,
  ...props
}) => {
  const baseClasses = 'font-semibold border-2 rounded-lg cursor-pointer transition-all duration-300 focus:outline-none'

  const sizeClasses = {
    sm: 'py-1 px-3 text-sm',
    md: 'py-2 px-4 text-base',
    lg: 'py-3 px-6 text-lg'
  }

  const widthClass = fullWidth ? 'w-full' : ''
  const disabledClass = (disabled || loading) ? 'opacity-50 cursor-not-allowed' : ''

  // Button styling for each variant matching session card design
  const variantClasses = {
    primary: 'border-emerald-700 bg-gradient-to-r from-emerald-50 to-amber-50 text-stone-800 hover:shadow-md shadow-sm',
    secondary: 'border-stone-200 bg-white text-stone-700 hover:border-amber-300 hover:shadow-sm',
    danger: 'border-red-600 bg-gradient-to-r from-red-50 to-red-100 text-red-700 hover:shadow-md shadow-sm',
    outline: 'border-stone-300 bg-white text-stone-700 hover:border-emerald-600 hover:bg-emerald-50 hover:shadow-sm'
  }

  const spinnerColor = {
    primary: 'text-stone-800',
    secondary: 'text-stone-700',
    danger: 'text-red-700',
    outline: 'text-stone-700'
  }

  return (
    <button
      className={`${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${widthClass} ${disabledClass} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <span className="flex items-center justify-center">
          <svg className={`animate-spin -ml-1 mr-2 h-4 w-4 ${spinnerColor[variant]}`} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Loading...
        </span>
      ) : children}
    </button>
  )
}
