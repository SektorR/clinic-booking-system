# Ground & Grow Psychology - Demo Version

🎉 **Live Demo:** [https://sektorr.github.io/clinic-booking-system/](https://sektorr.github.io/clinic-booking-system/)

## Overview

This is a fully functional **demonstration version** of the Ground & Grow Psychology booking platform that works entirely in the browser without requiring a backend server. All data is stored locally using browser localStorage.

## Features Demonstrated

### ✅ Patient Portal (Public Access)
- **Browse Psychologists**: View 5 mock psychologists with different specializations
- **View Availability**: Check real-time available time slots (9 AM - 5 PM weekdays)
- **Book Appointments**: Complete multi-step booking process
  - Select session type (Initial Consultation, Standard Session, Extended Session, etc.)
  - Choose date and time
  - Enter personal information
  - Review and confirm
- **Mock Payment**: Simulated Stripe payment (no actual payment processed)
- **Manage Bookings**: View, reschedule, or cancel bookings using confirmation tokens
- **Refund Policy**: Cancellations more than 24 hours before appointment show mock refund

### 🔒 Psychologist Portal (Not Available in Demo)
The psychologist dashboard and authenticated features require a backend server and are not functional in this demo version.

## How to Use the Demo

### 1. Browse Psychologists
- Click "Book an Appointment" on the home page
- Browse through 5 psychologists with different specializations:
  - Dr. Sarah Mitchell - Clinical Psychology, Anxiety & Depression
  - Dr. James Chen - Trauma & PTSD, Family Therapy
  - Dr. Emma Rodriguez - Child & Adolescent Psychology, ADHD
  - Dr. Michael Thompson - Addiction & Recovery, Mindfulness
  - Dr. Lisa Nguyen - Relationship Counseling, LGBTQ+ Support

### 2. Book an Appointment
1. Select a psychologist
2. Choose your session type:
   - Initial Consultation (60 min) - $150
   - Standard Session (50 min) - $120
   - Extended Session (90 min) - $200
   - In-Person Session (50 min) - $130
   - Phone Consultation (30 min) - $80
3. Pick a date and time from available slots
4. Fill in your details (first name, last name, email, phone)
5. Review and confirm
6. Payment is automatically "completed" (simulated)

### 3. Manage Your Booking
After booking, you'll see a link to manage your appointment. You can:
- View booking details
- Reschedule to a different date/time
- Cancel booking (mock refund if >24 hours notice)

### 4. Test Multiple Bookings
- Book multiple appointments to see time slots become unavailable
- Try different psychologists
- Test the cancel and reschedule features

## Technical Details

### Mock Data
- **5 Psychologists** with realistic profiles
- **5 Session Types** with different durations and prices
- **Dynamic availability** generation (9 AM - 5 PM on weekdays, limited weekend slots)
- **localStorage** for persistent bookings across browser sessions

### What's Mocked
- ✅ Psychologist data
- ✅ Session types/services
- ✅ Availability checking
- ✅ Booking creation
- ✅ Payment processing (Stripe)
- ✅ Email/SMS notifications (UI only)
- ✅ Booking management (view/cancel/reschedule)

### What's NOT Included
- ❌ Psychologist authentication/login
- ❌ Psychologist dashboard
- ❌ Real payment processing
- ❌ Email/SMS sending
- ❌ Backend API
- ❌ Database

## Technology Stack

- **Frontend Framework**: React 18.2.0 with TypeScript 5.2.2
- **Build Tool**: Vite 5.0.8
- **Styling**: Tailwind CSS 3.4.0
- **State Management**: Redux Toolkit 2.0.1
- **Routing**: React Router DOM 6.21.0
- **Form Handling**: React Hook Form 7.49.0
- **Date Handling**: date-fns 3.0.0
- **Storage**: Browser localStorage

## Local Development

### Prerequisites
- Node.js 18+ installed
- npm or yarn package manager

### Run Locally
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Open browser to http://localhost:5173
```

### Build for Production
```bash
cd frontend
npm run build
# Output in frontend/dist/
```

## Data Persistence

All bookings are stored in your browser's localStorage:
- Key: `mockBookings`
- Format: JSON array of booking objects
- Persists across browser sessions
- Clear browser data to reset

## Clearing Demo Data

To reset all bookings:
1. Open browser DevTools (F12)
2. Go to Console tab
3. Run: `localStorage.clear()`
4. Refresh the page

## Limitations

This is a **demonstration version** with the following limitations:
- No real backend server
- No actual payment processing
- No email/SMS notifications sent
- Data only persists in your browser
- No user authentication
- No data sync across devices
- Psychologist portal is non-functional

## Full Version Features

The complete production version includes:
- **Backend**: Spring Boot REST API
- **Database**: MongoDB for data persistence
- **Authentication**: JWT-based secure login
- **Payment**: Real Stripe integration
- **Notifications**: Email (SendGrid) and SMS (Twilio)
- **Psychologist Dashboard**: Appointment management, availability settings, client messaging
- **Admin Portal**: System administration
- **Production Deployment**: Docker containers, cloud hosting

## Questions or Issues?

For the full-featured version or questions about implementation:
- Repository: [https://github.com/SektorR/clinic-booking-system](https://github.com/SektorR/clinic-booking-system)
- Architecture Docs: See `/SYSTEM_ARCHITECTURE.md`
- Feature Docs: See `/FEATURES.md`

---

**Enjoy the demo! 🌱**
