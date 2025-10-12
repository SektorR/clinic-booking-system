import React from 'react'
import { Link } from 'react-router-dom'

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gradient-to-br from-stone-800 via-stone-700 to-emerald-900 text-amber-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-lg font-semibold mb-4 text-amber-50">Ground & Grow Psychology</h3>
            <p className="text-stone-300 text-sm">
              Professional psychological services in Australia. Online, in-person, and phone consultations available.
            </p>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4 text-amber-50">Quick Links</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/psychologists" className="text-stone-300 hover:text-amber-100 transition-colors duration-300">
                  Find a Psychologist
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-stone-300 hover:text-amber-100 transition-colors duration-300">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-stone-300 hover:text-amber-100 transition-colors duration-300">
                  Contact
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4 text-amber-50">Legal</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/privacy" className="text-stone-300 hover:text-amber-100 transition-colors duration-300">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link to="/terms" className="text-stone-300 hover:text-amber-100 transition-colors duration-300">
                  Terms of Service
                </Link>
              </li>
            </ul>
          </div>
        </div>
        <div className="mt-8 pt-8 border-t border-stone-600 text-center text-sm text-stone-300">
          <p>&copy; {new Date().getFullYear()} Ground & Grow Psychology. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}
