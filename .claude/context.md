# Ground & Grow Psychology - Project Context

## Project Overview
**Ground & Grow Psychology** is an Australian psychology service provider specializing in mental health and wellbeing support.

## Services Provided
- **Online Counseling** - Virtual therapy sessions
- **In-Person Counseling** - Face-to-face consultations at physical locations
- **Phone Counseling** - Telephone-based psychological services

## Our Role
We are the **software and technology team** responsible for:
- Building and maintaining technical infrastructure
- Leveraging AI and modern technology to enhance service delivery
- Ensuring the clinic can effectively utilize tech to better serve their clients
- Creating tools and systems that support both clinicians and clients

## Target Market
Australian-based clients seeking psychological services and mental health support.

## Tech Stack

### Frontend
- **HTML5** - Semantic markup
- **TypeScript** - Type-safe JavaScript for better code quality and AI agent compatibility
- **React** - Component-based UI framework
- **React Router** - Client-side routing for dual portals
- **Redux Toolkit** - State management
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client for API calls

### Backend
- **Java 17+** - Core programming language
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication and authorization (psychologist portal only)
- **Spring Data MongoDB** - Database integration
- **Maven** - Build and dependency management
- **JWT** - Token-based authentication (psychologist portal only)
- **Lombok** - Reduce boilerplate code
- **Stripe** - Payment processing
- **Twilio** - SMS notifications
- **SendGrid / JavaMail** - Email notifications
- **Thymeleaf** - Email templates

### Database
- **MongoDB** - NoSQL document database
- **MongoDB Atlas** - Cloud database hosting (recommended)

### DevOps & Tools
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Git** - Version control
- **GitHub Actions** - CI/CD (future)
- **Postman/Swagger** - API documentation and testing

### Testing
- **JUnit 5** - Java unit testing
- **Mockito** - Mocking framework
- **Jest** - JavaScript testing
- **React Testing Library** - Component testing

## Application Architecture

### Monorepo Structure
```
groundandgrow/
├── backend/          # Spring Boot application
├── frontend/         # React application
└── docker/           # Docker configurations
```

### Dual Portal System
Full-stack web application with two distinct user interfaces:

1. **Patient Portal** (`/patient`) - **NO LOGIN REQUIRED**
   - **Guest Booking System** - Patients can book appointments without creating an account
   - Browse available psychologists and time slots
   - Select appointment type (online/in-person/phone)
   - Provide basic information (name, email, phone)
   - **Stripe Payment Integration** - Pay for session at time of booking
   - Receive booking confirmation via email and SMS
   - Manage booking via unique confirmation token (sent via email)
   - Cancel or reschedule appointments using confirmation link
   - Automatic appointment reminders (24 hours before)
   - Optional: Create account to view booking history (future feature)

2. **Psychologist Portal** (`/psychologist`) - **LOGIN REQUIRED**
   - **JWT-based authentication** - Secure login for professionals
   - Professional dashboard with today's appointments
   - Calendar and availability management
   - View and manage client appointments (both guest and registered)
   - Client notes and session documentation
   - Schedule management (set working hours, time off, recurring availability)
   - View payment history and session summaries
   - Secure messaging system with clients
   - Analytics and reporting

### API Architecture
- **RESTful API** design
- **OpenAPI/Swagger** documentation
- **JWT-based** authentication
- **Role-based** access control (RBAC)
- **CORS** enabled for frontend-backend communication

## Key Features

### Payment System
- **Stripe Integration** - Secure payment processing for all bookings
- **AUD Currency** - Australian Dollar support
- **Immediate Payment** - Patients pay at time of booking (no login required)
- **Refund Support** - Automated refunds for cancellations (based on cancellation policy)
- **Payment Confirmation** - Email and SMS notifications for successful payments

### Notification System
- **Email Notifications** (JavaMail or SendGrid)
  - Booking confirmation with appointment details
  - Payment receipt
  - Appointment reminders (24 hours before)
  - Cancellation confirmations
  - Rescheduling confirmations
  - HTML email templates using Thymeleaf

- **SMS Notifications** (Twilio)
  - Booking confirmation with appointment details
  - Appointment reminders
  - Cancellation alerts
  - Australian phone number formatting (+61)
  - Verification codes for booking management

### Communication System
- **Secure Messaging** - Between psychologists and clients
- **Thread-based Conversations** - Organized message history
- **Appointment-linked Messages** - Messages tied to specific appointments
- **Read Receipts** - Track message status
- **System Notifications** - Automated updates about appointments

### Guest Booking Flow
1. Patient browses available psychologists and time slots
2. Selects appointment type and time
3. Provides basic information (name, email, phone)
4. Proceeds to Stripe payment
5. Upon successful payment:
   - Booking is confirmed
   - Unique confirmation token generated
   - Email sent with booking details and management link
   - SMS sent with appointment confirmation
6. Patient receives reminder 24 hours before appointment
7. Patient can cancel/reschedule via confirmation link (no login needed)

## Technical Goals
- Integrate AI capabilities to improve client experience
- Streamline clinic operations through automation
- Provide scalable, secure, and compliant solutions for healthcare delivery
- Enable efficient scheduling, communication, and service delivery across all modalities (online/in-person/phone)
- Ensure frictionless booking experience (no login required for patients)
- Maintain HIPAA/Australian privacy compliance for sensitive health data
