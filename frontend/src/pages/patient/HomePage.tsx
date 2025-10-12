import React from 'react'
import { Link } from 'react-router-dom'
import { Button, Card } from '../../components/common'

export const HomePage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 via-stone-50 to-emerald-50 relative">
      {/* Left Side - Oak Tree Branches */}
      <div className="hidden lg:block fixed left-0 top-0 bottom-0 w-48 xl:w-64 pointer-events-none opacity-25">
        <svg className="absolute top-0 left-0 h-full w-full" viewBox="0 0 280 1000" fill="none" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMinYMin meet">
          {/* Far background Oak - smallest */}
          <path d="M 200 1000 Q 198 800 196 600 Q 194 400 192 200 Q 191 100 190 50" stroke="#7a6d5f" strokeWidth="3" fill="none" opacity="0.3"/>
          <path d="M 196 280 Q 208 265 225 255" stroke="#8a7d6f" strokeWidth="2" fill="none" opacity="0.3"/>
          <path d="M 194 520 Q 206 505 223 495" stroke="#8a7d6f" strokeWidth="2" fill="none" opacity="0.3"/>
          <circle cx="227" cy="252" r="14" fill="#6b9d7a" opacity="0.3"/>
          <circle cx="225" cy="492" r="16" fill="#5a8c69" opacity="0.3"/>

          {/* Middle background Oak */}
          <path d="M 140 1000 Q 138 800 136 600 Q 134 400 132 200 Q 131 100 130 40" stroke="#6b5d4f" strokeWidth="5" fill="none" opacity="0.4"/>
          <path d="M 136 220 Q 150 205 175 190" stroke="#7a6d5f" strokeWidth="3" fill="none" opacity="0.4"/>
          <path d="M 134 450 Q 148 435 173 420" stroke="#7a6d5f" strokeWidth="3" fill="none" opacity="0.4"/>
          <path d="M 136 700 Q 152 685 177 670" stroke="#7a6d5f" strokeWidth="3" fill="none" opacity="0.4"/>
          <circle cx="177" cy="187" r="18" fill="#5a8c69" opacity="0.4"/>
          <circle cx="175" cy="417" r="20" fill="#4a7c59" opacity="0.4"/>
          <circle cx="179" cy="667" r="19" fill="#6b9d7a" opacity="0.4"/>

          {/* Front Oak trunk - tallest */}
          <path d="M 20 1000 Q 25 750 30 500 Q 35 300 38 150 Q 40 50 42 10 Q 43 5 44 0" stroke="#5d4e37" strokeWidth="8" fill="none"/>
          {/* Front Oak branches */}
          <path d="M 30 180 Q 50 160 90 140" stroke="#6b5d4f" strokeWidth="4" fill="none"/>
          <path d="M 35 320 Q 60 300 100 280" stroke="#6b5d4f" strokeWidth="5" fill="none"/>
          <path d="M 30 500 Q 55 480 95 460" stroke="#6b5d4f" strokeWidth="4" fill="none"/>
          <path d="M 35 700 Q 65 680 105 660" stroke="#6b5d4f" strokeWidth="5" fill="none"/>
          <path d="M 32 850 Q 60 830 100 810" stroke="#6b5d4f" strokeWidth="4" fill="none"/>
          {/* Front Oak leaves */}
          <circle cx="95" cy="135" r="25" fill="#4a7c59" opacity="0.7"/>
          <circle cx="105" cy="275" r="30" fill="#5a8c69" opacity="0.7"/>
          <circle cx="100" cy="455" r="28" fill="#4a7c59" opacity="0.7"/>
          <circle cx="110" cy="655" r="32" fill="#5a8c69" opacity="0.7"/>
          <circle cx="105" cy="805" r="27" fill="#6b9d7a" opacity="0.7"/>
        </svg>
      </div>

      {/* Right Side - Birch Tree Branches */}
      <div className="hidden lg:block fixed right-0 top-0 bottom-0 w-48 xl:w-64 pointer-events-none opacity-25">
        <svg className="absolute top-0 right-0 h-full w-full" viewBox="0 0 280 1000" fill="none" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMaxYMin meet">
          {/* Far background Birch - smallest */}
          <path d="M 60 1000 Q 58 800 56 600 Q 54 400 52 200 Q 51 100 50 45" stroke="#f0ebe0" strokeWidth="3" fill="none" opacity="0.3"/>
          <rect x="48" y="180" width="2" height="10" fill="#3d3d3d" opacity="0.2"/>
          <rect x="48" y="420" width="3" height="12" fill="#3d3d3d" opacity="0.2"/>
          <path d="M 56 240 Q 40 225 25 215" stroke="#e5e0d5" strokeWidth="2" fill="none" opacity="0.3"/>
          <path d="M 54 480 Q 38 465 23 455" stroke="#e5e0d5" strokeWidth="2" fill="none" opacity="0.3"/>
          <ellipse cx="23" cy="210" rx="14" ry="18" fill="#8db88b" opacity="0.3"/>
          <ellipse cx="21" cy="450" rx="15" ry="19" fill="#7da87b" opacity="0.3"/>

          {/* Middle background Birch */}
          <path d="M 140 1000 Q 138 800 136 600 Q 134 400 132 200 Q 131 100 130 35" stroke="#ebe7dc" strokeWidth="5" fill="none" opacity="0.4"/>
          <rect x="127" y="160" width="3" height="12" fill="#3d3d3d" opacity="0.3"/>
          <rect x="127" y="360" width="4" height="14" fill="#3d3d3d" opacity="0.3"/>
          <rect x="128" y="600" width="3" height="11" fill="#3d3d3d" opacity="0.3"/>
          <path d="M 136 200 Q 118 185 95 170" stroke="#ddd9ce" strokeWidth="2.5" fill="none" opacity="0.4"/>
          <path d="M 134 400 Q 116 385 93 370" stroke="#ddd9ce" strokeWidth="2.5" fill="none" opacity="0.4"/>
          <path d="M 136 640 Q 120 625 97 610" stroke="#ddd9ce" strokeWidth="2.5" fill="none" opacity="0.4"/>
          <ellipse cx="93" cy="165" rx="16" ry="20" fill="#8db88b" opacity="0.4"/>
          <ellipse cx="91" cy="365" rx="18" ry="22" fill="#7da87b" opacity="0.4"/>
          <ellipse cx="95" cy="605" rx="17" ry="21" fill="#6b9d7a" opacity="0.4"/>

          {/* Front Birch trunk - tallest */}
          <path d="M 250 1000 Q 245 750 240 500 Q 235 300 232 150 Q 230 50 228 10 Q 227 5 226 0" stroke="#e8e4d9" strokeWidth="7" fill="none"/>
          {/* Front Birch bark markings */}
          <rect x="238" y="100" width="4" height="15" fill="#3d3d3d" opacity="0.4"/>
          <rect x="237" y="250" width="6" height="20" fill="#3d3d3d" opacity="0.3"/>
          <rect x="238" y="420" width="5" height="18" fill="#3d3d3d" opacity="0.4"/>
          <rect x="239" y="600" width="4" height="12" fill="#3d3d3d" opacity="0.3"/>
          <rect x="238" y="780" width="5" height="16" fill="#3d3d3d" opacity="0.4"/>
          {/* Front Birch branches */}
          <path d="M 240 160 Q 205 140 170 120" stroke="#d4cfc4" strokeWidth="3" fill="none"/>
          <path d="M 235 300 Q 200 280 165 260" stroke="#d4cfc4" strokeWidth="4" fill="none"/>
          <path d="M 240 480 Q 210 465 180 450" stroke="#d4cfc4" strokeWidth="3" fill="none"/>
          <path d="M 235 670 Q 200 650 165 630" stroke="#d4cfc4" strokeWidth="4" fill="none"/>
          <path d="M 238 840 Q 208 820 178 800" stroke="#d4cfc4" strokeWidth="3" fill="none"/>
          {/* Front Birch leaves */}
          <ellipse cx="165" cy="115" rx="20" ry="25" fill="#7da87b" opacity="0.6"/>
          <ellipse cx="160" cy="255" rx="25" ry="30" fill="#8db88b" opacity="0.6"/>
          <ellipse cx="175" cy="445" rx="22" ry="28" fill="#7da87b" opacity="0.6"/>
          <ellipse cx="160" cy="625" rx="26" ry="32" fill="#8db88b" opacity="0.6"/>
          <ellipse cx="173" cy="795" rx="23" ry="27" fill="#6b9d7a" opacity="0.6"/>
        </svg>
      </div>

      {/* Bottom Left Bushes */}
      <div className="hidden lg:block fixed bottom-0 left-0 w-60 xl:w-80 h-32 pointer-events-none opacity-30">
        <svg className="absolute bottom-0 left-0 w-full h-full" viewBox="0 0 320 128" fill="none" xmlns="http://www.w3.org/2000/svg">
          {/* Bush cluster 1 */}
          <ellipse cx="40" cy="90" rx="35" ry="38" fill="#5a8c69" opacity="0.7"/>
          <ellipse cx="60" cy="95" rx="30" ry="33" fill="#4a7c59" opacity="0.7"/>
          <ellipse cx="25" cy="98" rx="28" ry="30" fill="#6b9d7a" opacity="0.6"/>
          {/* Bush cluster 2 */}
          <ellipse cx="130" cy="95" rx="32" ry="33" fill="#4a7c59" opacity="0.6"/>
          <ellipse cx="150" cy="100" rx="28" ry="28" fill="#5a8c69" opacity="0.6"/>
          {/* Small ground plants */}
          <circle cx="90" cy="110" r="12" fill="#6b9d7a" opacity="0.5"/>
          <circle cx="200" cy="112" r="10" fill="#5a8c69" opacity="0.5"/>
        </svg>
      </div>

      {/* Bottom Right Bushes */}
      <div className="hidden lg:block fixed bottom-0 right-0 w-60 xl:w-80 h-32 pointer-events-none opacity-30">
        <svg className="absolute bottom-0 right-0 w-full h-full" viewBox="0 0 320 128" fill="none" xmlns="http://www.w3.org/2000/svg">
          {/* Bush cluster 1 */}
          <ellipse cx="280" cy="92" rx="33" ry="36" fill="#6b9d7a" opacity="0.7"/>
          <ellipse cx="260" cy="97" rx="30" ry="31" fill="#5a8c69" opacity="0.7"/>
          <ellipse cx="295" cy="100" rx="25" ry="28" fill="#4a7c59" opacity="0.6"/>
          {/* Bush cluster 2 */}
          <ellipse cx="190" cy="98" rx="30" ry="30" fill="#5a8c69" opacity="0.6"/>
          <ellipse cx="170" cy="103" rx="26" ry="25" fill="#6b9d7a" opacity="0.6"/>
          {/* Small ground plants */}
          <circle cx="230" cy="108" r="11" fill="#4a7c59" opacity="0.5"/>
          <circle cx="120" cy="115" r="9" fill="#6b9d7a" opacity="0.5"/>
        </svg>
      </div>

      {/* Hero Section */}
      <section className="bg-gradient-to-br from-stone-100/50 via-amber-50/40 to-emerald-50/30 relative z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-bold mb-6 pb-2 text-stone-800 drop-shadow-lg">
              Professional Psychology Services in Australia
            </h1>
            <p className="text-xl md:text-2xl mb-8 text-stone-700">
              Online, In-Person, and Phone Consultations Available
            </p>
            <Link to="/psychologists" className="inline-block group">
              <div className="relative">
                <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
                <div className="relative">
                  <Button size="lg">
                    Book an Appointment
                  </Button>
                </div>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12 pb-2 text-stone-800 drop-shadow-lg">How It Works</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card>
              <div className="text-center">
                <div className="inline-block p-1 rounded-full bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900 shadow-md mx-auto mb-4">
                  <div className="bg-white w-14 h-14 rounded-full flex items-center justify-center">
                    <span className="text-2xl font-bold text-stone-800">1</span>
                  </div>
                </div>
                <h3 className="text-xl font-semibold mb-2 text-stone-800">Choose a Psychologist</h3>
                <p className="text-stone-600">
                  Browse our qualified psychologists and find one that suits your needs.
                </p>
              </div>
            </Card>

            <Card>
              <div className="text-center">
                <div className="inline-block p-1 rounded-full bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900 shadow-md mx-auto mb-4">
                  <div className="bg-white w-14 h-14 rounded-full flex items-center justify-center">
                    <span className="text-2xl font-bold text-stone-800">2</span>
                  </div>
                </div>
                <h3 className="text-xl font-semibold mb-2 text-stone-800">Book & Pay</h3>
                <p className="text-stone-600">
                  Select your preferred time and modality. No account required - just book and pay securely.
                </p>
              </div>
            </Card>

            <Card>
              <div className="text-center">
                <div className="inline-block p-1 rounded-full bg-gradient-to-r from-stone-800 via-stone-700 to-emerald-900 shadow-md mx-auto mb-4">
                  <div className="bg-white w-14 h-14 rounded-full flex items-center justify-center">
                    <span className="text-2xl font-bold text-stone-800">3</span>
                  </div>
                </div>
                <h3 className="text-xl font-semibold mb-2 text-stone-800">Attend Your Session</h3>
                <p className="text-stone-600">
                  Receive confirmation via email and SMS. We'll remind you before your appointment.
                </p>
              </div>
            </Card>
          </div>
        </div>
      </section>

      {/* Service Types Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12 pb-2 text-stone-800 drop-shadow-lg">Our Services</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card hover>
              <h3 className="text-xl font-semibold mb-3 text-stone-800">Online Counseling</h3>
              <p className="text-stone-600 mb-4">
                Connect with our psychologists via secure video call from the comfort of your home.
              </p>
              <ul className="text-sm text-stone-500 space-y-1">
                <li>✓ Convenient and flexible</li>
                <li>✓ Secure video platform</li>
                <li>✓ Australia-wide availability</li>
              </ul>
            </Card>

            <Card hover>
              <h3 className="text-xl font-semibold mb-3 text-stone-800">In-Person Counseling</h3>
              <p className="text-stone-600 mb-4">
                Face-to-face consultations at our professional clinic locations.
              </p>
              <ul className="text-sm text-stone-500 space-y-1">
                <li>✓ Traditional therapy experience</li>
                <li>✓ Professional environment</li>
                <li>✓ Multiple locations</li>
              </ul>
            </Card>

            <Card hover>
              <h3 className="text-xl font-semibold mb-3 text-stone-800">Phone Counseling</h3>
              <p className="text-stone-600 mb-4">
                Receive support via telephone consultation with our experienced psychologists.
              </p>
              <ul className="text-sm text-stone-500 space-y-1">
                <li>✓ Quick and accessible</li>
                <li>✓ No video required</li>
                <li>✓ Call from anywhere</li>
              </ul>
            </Card>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gradient-to-br from-stone-100/50 via-amber-50/40 to-emerald-50/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold mb-6 pb-2 text-stone-800 drop-shadow-lg">Ready to Get Started?</h2>
          <p className="text-xl mb-8 text-stone-700">
            Book your first appointment today. No account required.
          </p>
          <Link to="/psychologists" className="inline-block group">
            <div className="relative">
              <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 rounded-lg blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500"></div>
              <div className="relative">
                <Button size="lg">
                  Find a Psychologist
                </Button>
              </div>
            </div>
          </Link>
        </div>
      </section>
    </div>
  )
}

export default HomePage
