# Ground & Grow Psychology - System Architecture & Operations Guide

**Version:** 1.0
**Last Updated:** 2025-10-05
**Target Audience:** AI Agents, Programmers, DevOps Engineers, System Architects

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architecture Patterns](#architecture-patterns)
4. [Infrastructure Components](#infrastructure-components)
5. [Backend System Architecture](#backend-system-architecture)
6. [Frontend System Architecture](#frontend-system-architecture)
7. [Database Schema & Design](#database-schema--design)
8. [API Architecture](#api-architecture)
9. [Authentication & Authorization](#authentication--authorization)
10. [Payment Processing System](#payment-processing-system)
11. [Notification System](#notification-system)
12. [Messaging System](#messaging-system)
13. [Availability & Scheduling System](#availability--scheduling-system)
14. [Data Flow Diagrams](#data-flow-diagrams)
15. [Deployment Architecture](#deployment-architecture)
16. [Security Architecture](#security-architecture)
17. [Monitoring & Logging](#monitoring--logging)
18. [Performance Considerations](#performance-considerations)
19. [Scalability Strategies](#scalability-strategies)
20. [Disaster Recovery](#disaster-recovery)
21. [Development Workflow](#development-workflow)
22. [Testing Strategy](#testing-strategy)
23. [CI/CD Pipeline](#cicd-pipeline)
24. [Troubleshooting Guide](#troubleshooting-guide)
25. [Appendices](#appendices)

---

## Executive Summary

**Ground & Grow Psychology** is a full-stack web application designed for managing psychology services in Australia. The platform provides a dual-portal system:
- **Patient Portal:** Frictionless guest booking system without account creation
- **Psychologist Portal:** Authenticated dashboard for managing appointments, availability, and client communication

### Key Architectural Decisions
- **Microservices Ready:** Modular service layer enables future microservices decomposition
- **Stateless API:** JWT-based authentication with no server-side sessions
- **Event-Driven Notifications:** Scheduled background tasks for automated reminders
- **No-Account Booking:** Token-based booking management reduces friction
- **Payment-First Design:** All bookings require immediate payment via Stripe
- **Multi-Channel Notifications:** Email and SMS via external providers (SendGrid, Twilio)

### Technology Stack
| Layer | Technology | Version |
|-------|-----------|---------|
| Backend Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Frontend Framework | React | 18.2.0 |
| Language | TypeScript | 5.2.2 |
| Database | MongoDB | 6.0+ |
| Authentication | JWT | JJWT 0.12.3 |
| Payment Processing | Stripe | 24.16.0 |
| Email | JavaMail / SendGrid | 1.6.2 / 4.10.1 |
| SMS | Twilio | 9.14.1 |
| State Management | Redux Toolkit | 2.0.1 |
| API Documentation | SpringDoc OpenAPI | 2.3.0 |
| Build Tool (Backend) | Maven | 3.8+ |
| Build Tool (Frontend) | Vite | 5.0.8 |

---

## System Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET / CDN                               │
└────────────┬────────────────────────────────────────────────────────┘
             │
     ┌───────┴────────┐
     │   Load Balancer │  (Future: Nginx, AWS ALB)
     └───────┬────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────┐      ┌────▼────┐
│Frontend│      │ Backend │
│ React  │◄────►│ Spring  │
│  App   │      │  Boot   │
└────────┘      └────┬────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
    ┌───▼───┐   ┌───▼───┐   ┌───▼───────┐
    │MongoDB│   │Stripe │   │Email/SMS  │
    │       │   │  API  │   │ Providers │
    └───────┘   └───────┘   └───────────┘
```

### Component Responsibilities

#### Frontend (React TypeScript)
- **Patient Portal:**
  - Browse psychologists
  - Book appointments (guest)
  - Manage bookings via token
  - Payment checkout (Stripe redirect)

- **Psychologist Portal:**
  - Login/Authentication
  - Dashboard (appointments, stats)
  - Manage availability
  - View clients
  - Secure messaging

#### Backend (Spring Boot)
- **Core Services:**
  - User authentication (JWT)
  - Guest booking management
  - Payment processing (Stripe)
  - Notification scheduling
  - Availability calculation
  - Messaging

- **External Integrations:**
  - Stripe (payment processing)
  - Twilio (SMS)
  - JavaMail/SendGrid (Email)

#### Database (MongoDB)
- **Collections:**
  - psychologists
  - clients (future feature)
  - guest_bookings
  - session_types
  - availability
  - time_off
  - messages
  - notifications

---

## Architecture Patterns

### 1. Layered Architecture (Backend)

```
┌─────────────────────────────────────────────┐
│          Controller Layer                    │  REST endpoints, request validation
├─────────────────────────────────────────────┤
│          Service Layer                       │  Business logic, transactions
├─────────────────────────────────────────────┤
│          Repository Layer                    │  Data access, MongoDB queries
├─────────────────────────────────────────────┤
│          Model Layer                         │  Domain entities, MongoDB documents
└─────────────────────────────────────────────┘
```

**Benefits:**
- Separation of concerns
- Testability (each layer can be unit tested)
- Maintainability (changes isolated to specific layers)

### 2. Component-Based Architecture (Frontend)

```
┌─────────────────────────────────────────────┐
│          Pages (Route Components)            │  Top-level page views
├─────────────────────────────────────────────┤
│          Components                          │  Reusable UI elements
├─────────────────────────────────────────────┤
│          Services                            │  API communication
├─────────────────────────────────────────────┤
│          Redux Store                         │  Global state management
└─────────────────────────────────────────────┘
```

### 3. Repository Pattern

All database access goes through repository interfaces:
```java
@Repository
public interface GuestBookingRepository extends MongoRepository<GuestBooking, String> {
    Optional<GuestBooking> findByConfirmationToken(String token);
    List<GuestBooking> findByEmail(String email);
    // ... custom query methods
}
```

**Benefits:**
- Abstraction from database implementation
- Easy to mock for testing
- Supports custom query methods

### 4. DTO Pattern

Data Transfer Objects separate API contracts from domain models:
```java
// Domain Model
@Document(collection = "psychologists")
public class Psychologist {
    private String id;
    private String password;  // Never exposed in DTOs
    // ... other fields
}

// DTO (no password field)
public class PsychologistDTO {
    private String id;
    private String firstName;
    private String lastName;
    // ... safe fields only
}
```

### 5. Service-Oriented Design

Services encapsulate business logic and can be composed:
```java
@Service
public class GuestBookingService {
    @Autowired private StripeService stripeService;
    @Autowired private EmailService emailService;
    @Autowired private SmsService smsService;
    @Autowired private NotificationSchedulerService notificationService;

    public CheckoutSessionResponse createBooking(GuestBookingRequest request) {
        // Orchestrates multiple services
    }
}
```

---

## Infrastructure Components

### Development Environment

```
┌─────────────────────────────────────────────────────────────┐
│  Developer Machine                                           │
│                                                              │
│  ┌──────────────┐           ┌──────────────┐               │
│  │   Frontend   │           │   Backend    │               │
│  │   Vite Dev   │◄─────────►│  Spring Boot │               │
│  │  Port: 5173  │           │  Port: 8080  │               │
│  └──────────────┘           └──────┬───────┘               │
│                                    │                        │
│                             ┌──────▼────────┐              │
│                             │  MongoDB      │              │
│                             │  Port: 27017  │              │
│                             └───────────────┘              │
└─────────────────────────────────────────────────────────────┘
```

### Production Environment (Recommended)

```
┌──────────────────────────────────────────────────────────────────┐
│  Cloud Provider (AWS / Azure / GCP)                               │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  Load Balancer (ALB / Application Gateway)                 │  │
│  └───────────────┬──────────────────────────────────────────┬─┘  │
│                  │                                          │    │
│        ┌─────────▼─────────┐                    ┌──────────▼─────┐│
│        │  Static Files     │                    │  API Servers   ││
│        │  (S3 / Blob)      │                    │  (EC2 / VMs)   ││
│        │  Frontend React   │                    │  Spring Boot   ││
│        │  CloudFront CDN   │                    │  Auto-scaling  ││
│        └───────────────────┘                    └────────┬───────┘│
│                                                          │        │
│        ┌──────────────────────────────────────┬─────────▼───────┐│
│        │  Database Cluster                     │  External APIs │││
│        │  MongoDB Atlas                        │  • Stripe      │││
│        │  • Primary + Replicas                 │  • Twilio      │││
│        │  • Auto-backup                        │  • SendGrid    │││
│        └───────────────────────────────────────┴────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

### Docker Configuration

Located in `docker/docker-compose.yml`:
```yaml
services:
  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
    environment:
      - MONGODB_URI=mongodb://mongodb:27017/groundandgrow

  frontend:
    build: ./frontend
    ports:
      - "5173:80"
    depends_on:
      - backend
```

---

## Backend System Architecture

### Package Structure

```
com.groundandgrow/
├── config/
│   └── DataInitializer.java            # Database seeding
├── controller/
│   ├── AuthController.java             # Authentication endpoints
│   ├── PublicPsychologistController.java
│   ├── GuestBookingController.java
│   ├── StripeWebhookController.java
│   ├── PsychologistController.java
│   ├── AvailabilityController.java
│   ├── ClientController.java
│   ├── MessageController.java
│   └── NotificationController.java
├── dto/                                # 22 Data Transfer Objects
├── model/                              # 8 MongoDB entities
├── repository/                         # 8 MongoDB repositories
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConfig.java
│   ├── UserDetailsServiceImpl.java
│   └── UserPrincipal.java
├── service/
│   ├── AuthService.java
│   ├── GuestBookingService.java
│   ├── PsychologistService.java
│   ├── AvailabilityService.java
│   ├── AvailabilityManagementService.java
│   ├── SessionTypeService.java
│   ├── ClientManagementService.java
│   ├── StripeService.java
│   ├── EmailService.java
│   ├── SmsService.java
│   ├── MessageService.java
│   └── NotificationSchedulerService.java
└── GroundAndGrowApplication.java       # Main application class
```

### Service Interaction Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│  Controllers (REST API)                                           │
└──────────┬───────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Service Layer                                                    │
│                                                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌─────────────────┐   │
│  │GuestBooking  │───►│ Stripe       │    │Notification     │   │
│  │Service       │    │ Service      │    │Scheduler        │   │
│  └──────┬───────┘    └──────────────┘    │Service          │   │
│         │                                  └────────┬────────┘   │
│         ├────────────────────────────┐             │            │
│         │                            │             │            │
│         ▼                            ▼             ▼            │
│  ┌──────────────┐    ┌──────────────┐    ┌─────────────────┐   │
│  │Availability  │    │  Email       │    │  SMS            │   │
│  │Service       │    │  Service     │    │  Service        │   │
│  └──────────────┘    └──────────────┘    └─────────────────┘   │
└──────────┬───────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Repository Layer (MongoDB)                                       │
└──────────────────────────────────────────────────────────────────┘
```

### Key Services Explained

#### 1. GuestBookingService
**Purpose:** Orchestrates the complete booking lifecycle

**Key Methods:**
- `createBooking()` - Validates availability, creates booking, generates Stripe checkout
- `handlePaymentSuccess()` - Updates booking, sends confirmations, schedules reminders
- `handlePaymentFailure()` - Marks booking as failed
- `cancelBooking()` - Processes refunds, sends cancellation notifications
- `rescheduleBooking()` - Updates appointment time

**Dependencies:**
- StripeService (payment processing)
- AvailabilityService (slot validation)
- EmailService (confirmations)
- SmsService (notifications)
- NotificationSchedulerService (reminders)

#### 2. AvailabilityService
**Purpose:** Calculates available time slots in real-time

**Algorithm:**
1. Get psychologist's recurring availability for day of week
2. Check if date falls within time off period
3. Retrieve existing bookings for that day
4. Generate potential slots within availability blocks
5. Filter out slots that conflict with bookings
6. Return only future slots (not in the past)

**Complexity:** O(n * m) where n = availability blocks, m = existing bookings

#### 3. NotificationSchedulerService
**Purpose:** Automated notification processing with retry logic

**Scheduled Task:**
```java
@Scheduled(fixedDelay = 60000) // Every 60 seconds
public void processPendingNotifications() {
    // Query: status=PENDING and scheduledFor <= NOW
    // Send via Email/SMS
    // Update status to SENT or FAILED
    // Retry up to 3 times with 5-minute intervals
}
```

**Notification Types:**
- BOOKING_CONFIRMATION (immediate)
- REMINDER (24 hours before appointment)
- CANCELLATION (immediate)
- RESCHEDULED (immediate)
- MESSAGE_RECEIVED (immediate)
- PAYMENT_CONFIRMATION (immediate)

#### 4. StripeService
**Purpose:** Stripe payment processing

**Key Methods:**
- `createCheckoutSession()` - Creates Stripe hosted checkout
- `processRefund()` - Refunds payment via Stripe API
- `verifyWebhookSignature()` - Validates webhook authenticity

**Webhook Events Handled:**
- `checkout.session.completed`
- `checkout.session.expired`
- `payment_intent.succeeded`
- `payment_intent.payment_failed`
- `charge.refunded`

---

## Frontend System Architecture

### Folder Structure

```
frontend/src/
├── components/
│   ├── common/
│   │   ├── Navbar.tsx
│   │   ├── Footer.tsx
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Card.tsx
│   │   ├── Modal.tsx
│   │   ├── LoadingSpinner.tsx
│   │   └── index.ts
│   └── ProtectedRoute.tsx
├── pages/
│   ├── patient/
│   │   ├── HomePage.tsx
│   │   ├── PsychologistListPage.tsx
│   │   ├── BookingPage.tsx
│   │   ├── BookingSuccessPage.tsx
│   │   └── ManageBookingPage.tsx
│   └── psychologist/
│       ├── LoginPage.tsx
│       ├── DashboardPage.tsx
│       ├── AppointmentsPage.tsx
│       ├── AvailabilityPage.tsx
│       ├── ProfilePage.tsx
│       └── MessagesPage.tsx
├── services/
│   ├── api.ts                    # Axios instance with interceptors
│   ├── authService.ts            # Authentication API calls
│   ├── bookingService.ts         # Booking API calls
│   ├── psychologistService.ts    # Psychologist API calls
│   └── index.ts
├── store/
│   ├── store.ts                  # Redux store configuration
│   └── slices/
│       ├── authSlice.ts
│       └── bookingSlice.ts
├── types/
│   ├── auth.types.ts
│   ├── booking.types.ts
│   ├── psychologist.types.ts
│   └── index.ts
├── App.tsx                       # Main app with routing
└── main.tsx                      # Application entry point
```

### State Management (Redux)

```
Redux Store
├── auth/
│   ├── token: string | null
│   ├── user: { id, email, name } | null
│   └── isAuthenticated: boolean
└── booking/
    ├── currentBooking: GuestBooking | null
    ├── selectedPsychologist: Psychologist | null
    └── bookingStep: number
```

**State Flow:**
```
User Action (Login)
    │
    ▼
authService.login(email, password)
    │
    ▼
Backend API (/api/auth/login)
    │
    ▼
Response: { token, psychologistId, email, name }
    │
    ▼
dispatch(setCredentials({ token, user }))
    │
    ▼
Redux Store Updated
    │
    ▼
Components Re-render (useSelector)
```

### API Service Layer

**Axios Configuration (`api.ts`):**
```typescript
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
});

// Request Interceptor (adds JWT token)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor (handles 401 errors)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/psychologist/login';
    }
    return Promise.reject(error);
  }
);
```

### Routing Architecture

```
/                                   HomePage (public)
/psychologists                      PsychologistListPage (public)
/book/:psychologistId               BookingPage (public)
/booking/success                    BookingSuccessPage (public)
/booking/manage/:token              ManageBookingPage (public)

/psychologist/login                 LoginPage (public)
/psychologist/dashboard             DashboardPage (protected)
/psychologist/appointments          AppointmentsPage (protected)
/psychologist/availability          AvailabilityPage (protected)
/psychologist/messages              MessagesPage (protected)
/psychologist/profile               ProfilePage (protected)

*                                   404 Page
```

**Protected Route Implementation:**
```typescript
const ProtectedRoute = () => {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated)
  return isAuthenticated ? <Outlet /> : <Navigate to="/psychologist/login" replace />
}
```

---

## Database Schema & Design

### MongoDB Collections

#### 1. psychologists
```javascript
{
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String (unique, indexed),
  phone: String,
  password: String (hashed with BCrypt),
  role: String (enum: PSYCHOLOGIST, ADMIN),
  specialization: String,
  registrationNumber: String,
  bio: String,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `email` (unique)
- `isActive`

#### 2. session_types
```javascript
{
  _id: ObjectId,
  name: String,
  description: String,
  durationMinutes: Number,
  price: Number,
  modality: String (enum: ONLINE, IN_PERSON, PHONE),
  isActive: Boolean
}
```

**Indexes:**
- `modality, isActive` (compound)

#### 3. guest_bookings
```javascript
{
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String (indexed),
  phone: String,
  psychologistId: String (indexed),
  sessionTypeId: String,
  appointmentDateTime: Date (indexed),
  durationMinutes: Number,
  modality: String,
  notes: String,
  psychologistNotes: String,

  // Payment fields
  stripePaymentIntentId: String,
  stripeCheckoutSessionId: String (indexed),
  amount: Number,
  paymentStatus: String (enum: PENDING, COMPLETED, FAILED, REFUNDED),

  // Booking fields
  bookingStatus: String (enum: PENDING_PAYMENT, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW),
  confirmationToken: String (unique, indexed),
  emailConfirmed: Boolean,
  reminderSent: Boolean,

  // Metadata
  createdAt: Date,
  updatedAt: Date
}
```

**Indexes:**
- `email`
- `psychologistId`
- `confirmationToken` (unique)
- `stripeCheckoutSessionId`
- `appointmentDateTime`
- `psychologistId, appointmentDateTime` (compound)

#### 4. availability
```javascript
{
  _id: ObjectId,
  psychologistId: String (indexed),
  dayOfWeek: String (enum: MONDAY-SUNDAY),
  startTime: String (HH:mm format),
  endTime: String (HH:mm format),
  isRecurring: Boolean,
  effectiveFrom: Date,
  effectiveUntil: Date
}
```

**Indexes:**
- `psychologistId`
- `psychologistId, dayOfWeek` (compound)

#### 5. time_off
```javascript
{
  _id: ObjectId,
  psychologistId: String (indexed),
  startDateTime: Date,
  endDateTime: Date,
  reason: String,
  createdAt: Date
}
```

**Indexes:**
- `psychologistId`

#### 6. messages
```javascript
{
  _id: ObjectId,
  senderId: String (indexed),
  receiverId: String (indexed),
  senderType: String (enum: CLIENT, PSYCHOLOGIST, SYSTEM),
  receiverType: String (enum: CLIENT, PSYCHOLOGIST),
  subject: String,
  content: String,
  appointmentId: String,
  threadId: String (indexed),
  isRead: Boolean,
  readAt: Date,
  createdAt: Date
}
```

**Indexes:**
- `senderId`
- `receiverId`
- `threadId`
- `receiverId, isRead` (compound)

#### 7. notifications
```javascript
{
  _id: ObjectId,
  recipientId: String (indexed),
  recipientType: String (enum: CLIENT, PSYCHOLOGIST, GUEST),
  recipientEmail: String,
  recipientPhone: String,
  guestBookingId: String,
  notificationType: String (enum: BOOKING_CONFIRMATION, REMINDER, CANCELLATION, ...),
  deliveryMethod: String (enum: EMAIL, SMS, BOTH),
  subject: String,
  message: String,
  templateData: String (JSON),
  scheduledFor: Date (indexed),
  sentAt: Date,
  status: String (enum: PENDING, SENT, FAILED, CANCELLED),
  retryCount: Number,
  errorMessage: String,
  externalId: String,
  createdAt: Date
}
```

**Indexes:**
- `recipientId`
- `guestBookingId`
- `status, scheduledFor` (compound)

#### 8. clients (Future Feature)
```javascript
{
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String (unique, indexed),
  phone: String,
  password: String (hashed),
  dateOfBirth: Date,
  address: {
    street: String,
    city: String,
    state: String,
    postcode: String,
    country: String
  },
  emergencyContact: {
    name: String,
    phone: String,
    relationship: String
  },
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### Database Design Principles

1. **Denormalization for Performance:**
   - Store psychologist name in `guest_bookings` for quick display
   - Avoid joins by embedding related data

2. **Indexing Strategy:**
   - Index all frequently queried fields
   - Compound indexes for common query patterns
   - Unique indexes for tokens and emails

3. **Soft Deletes:**
   - Use `isActive` flags instead of hard deletes
   - Preserve historical data for analytics

4. **Audit Trail:**
   - `createdAt` and `updatedAt` on all documents
   - MongoDB's `@CreatedDate` and `@LastModifiedDate` annotations

---

## API Architecture

### REST API Design

**Base URL:** `http://localhost:8080/api`

**HTTP Methods:**
- `GET` - Retrieve resources
- `POST` - Create resources
- `PUT` - Update resources (full replacement)
- `PATCH` - Partial update (not used in current version)
- `DELETE` - Delete resources

**Response Format:**
```json
// Success (200 OK, 201 Created)
{
  "id": "abc123",
  "firstName": "John",
  "lastName": "Doe",
  ...
}

// Error (400 Bad Request, 401 Unauthorized, 404 Not Found, 500 Internal Server Error)
{
  "timestamp": "2025-10-05T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input: email is required",
  "path": "/api/public/bookings"
}
```

### API Versioning (Future)

Currently not versioned. Recommended approach for future:
- URL versioning: `/api/v1/bookings`, `/api/v2/bookings`
- Header versioning: `Accept: application/vnd.groundandgrow.v1+json`

### API Rate Limiting (Future)

Recommended implementation:
- Use `bucket4j` library
- Rate limit: 100 requests per minute per IP
- Protected endpoints: 60 requests per minute per user

---

## Authentication & Authorization

### JWT Token Flow

```
┌────────┐                                          ┌────────┐
│ Client │                                          │Backend │
└───┬────┘                                          └────┬───┘
    │                                                    │
    │  POST /api/auth/login                             │
    │  { email, password }                              │
    ├──────────────────────────────────────────────────►│
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Validate     │
    │                                     │ credentials  │
    │                                     │ (BCrypt)     │
    │                                     └──────────────┤
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Generate JWT │
    │                                     │ token        │
    │                                     │ (JJWT)       │
    │                                     └──────────────┤
    │                                                    │
    │  200 OK                                            │
    │  { token, psychologistId, email, name }           │
    │◄──────────────────────────────────────────────────┤
    │                                                    │
┌───┴────────────────────┐                              │
│ Store token in         │                              │
│ localStorage           │                              │
│ + Redux store          │                              │
└───┬────────────────────┘                              │
    │                                                    │
    │  GET /api/psychologist/dashboard                  │
    │  Authorization: Bearer eyJhbGci...                │
    ├──────────────────────────────────────────────────►│
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Extract      │
    │                                     │ token from   │
    │                                     │ header       │
    │                                     └──────────────┤
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Validate JWT │
    │                                     │ signature    │
    │                                     └──────────────┤
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Extract user │
    │                                     │ ID from      │
    │                                     │ claims       │
    │                                     └──────────────┤
    │                                                    │
    │                                     ┌──────────────┤
    │                                     │ Load user    │
    │                                     │ details      │
    │                                     └──────────────┤
    │                                                    │
    │  200 OK                                            │
    │  { dashboard data }                                │
    │◄──────────────────────────────────────────────────┤
    │                                                    │
```

### JWT Token Structure

**Header:**
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "psychologist-id-123",  // Subject (user ID)
  "email": "psychologist@example.com",
  "role": "PSYCHOLOGIST",
  "iat": 1696435200,             // Issued at
  "exp": 1696521600              // Expiration (24 hours)
}
```

**Signature:**
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### Security Configuration

**Public Endpoints (No Authentication):**
```java
"/api/public/**",
"/api/auth/login",
"/api/auth/refresh",
"/api/auth/validate",
"/api/webhooks/**",
"/swagger-ui/**",
"/v3/api-docs/**"
```

**Protected Endpoints (PSYCHOLOGIST Role):**
```java
"/api/psychologist/**",
"/api/messages/**",
"/api/notifications/**"
```

**Admin Endpoints (ADMIN Role):**
```java
"/api/auth/register",
"/api/admin/**"
```

### CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "http://localhost:3000",
        "https://groundandgrow.com.au"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setAllowCredentials(true);
    return source;
}
```

---

## Payment Processing System

### Stripe Integration Architecture

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│ Patient │                    │ Backend │                    │ Stripe  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │ 1. POST /api/public/bookings│                              │
     ├─────────────────────────────►│                              │
     │                              │                              │
     │                              │ 2. Create Checkout Session   │
     │                              ├─────────────────────────────►│
     │                              │                              │
     │                              │ 3. Return session URL        │
     │                              │◄─────────────────────────────┤
     │                              │                              │
     │ 4. Return checkout URL       │                              │
     │◄─────────────────────────────┤                              │
     │                              │                              │
     │ 5. Redirect to Stripe        │                              │
     ├──────────────────────────────┴──────────────────────────────►
     │                                                              │
     │ 6. Complete Payment                                         │
     ├──────────────────────────────────────────────────────────────►
     │                                                              │
     │                              │ 7. Webhook: checkout.session.completed
     │                              │◄─────────────────────────────┤
     │                              │                              │
     │                              │ 8. Verify signature          │
     │                              │    Update booking status     │
     │                              │    Send confirmations        │
     │                              │                              │
     │ 9. Redirect to success page  │                              │
     │◄─────────────────────────────────────────────────────────────┤
     │                              │                              │
```

### Stripe Checkout Session

**Creation:**
```java
public CheckoutSessionResponse createCheckoutSession(GuestBooking booking) {
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
        .setCancelUrl(cancelUrl)
        .addLineItem(
            SessionCreateParams.LineItem.builder()
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("aud")
                        .setUnitAmount(amount * 100)  // Amount in cents
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(sessionTypeName)
                                .setDescription(description)
                                .build()
                        )
                        .build()
                )
                .setQuantity(1L)
                .build()
        )
        .setPaymentIntentData(
            SessionCreateParams.PaymentIntentData.builder()
                .setMetadata(Map.of(
                    "bookingId", booking.getId(),
                    "confirmationToken", booking.getConfirmationToken()
                ))
                .build()
        )
        .build();

    Session session = Session.create(params);
    return new CheckoutSessionResponse(session.getUrl(), session.getId());
}
```

### Webhook Signature Verification

```java
@PostMapping("/stripe")
public ResponseEntity<String> handleWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature
) {
    try {
        Event event = Webhook.constructEvent(payload, signature, webhookSecret);

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailed(event);
                break;
            case "charge.refunded":
                handleRefund(event);
                break;
        }

        return ResponseEntity.ok("");
    } catch (SignatureVerificationException e) {
        return ResponseEntity.status(400).body("Invalid signature");
    }
}
```

### Refund Processing

```java
public void processRefund(String paymentIntentId, Long amount) {
    RefundCreateParams params = RefundCreateParams.builder()
        .setPaymentIntent(paymentIntentId)
        .setAmount(amount)
        .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
        .build();

    Refund refund = Refund.create(params);
}
```

**Refund Policy:**
- 24 hours before appointment: Full refund
- Less than 24 hours: No refund

---

## Notification System

### Notification Processing Flow

```
┌────────────────────────────────────────────────────────────────┐
│  Trigger Event (Booking Created, Payment Success, Cancellation)│
└───────────────────────┬────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│  NotificationSchedulerService.scheduleNotification()            │
│  • Generate notification record                                 │
│  • Set scheduledFor (immediate or 24h before)                  │
│  • Set status = PENDING                                        │
│  • Save to MongoDB notifications collection                    │
└───────────────────────┬────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│  @Scheduled Task (Every 60 seconds)                            │
│  processPendingNotifications()                                  │
│  • Query: status=PENDING AND scheduledFor <= NOW               │
│  • For each notification:                                      │
│    - Send via Email/SMS                                        │
│    - Update status to SENT or FAILED                           │
│    - Increment retryCount if failed                            │
│    - Schedule retry (max 3 attempts, 5-min intervals)          │
└───────────────────────┬────────────────────────────────────────┘
                        │
        ┌───────────────┴────────────────┐
        │                                │
        ▼                                ▼
┌──────────────────┐          ┌──────────────────┐
│  EmailService    │          │  SmsService      │
│  • JavaMail      │          │  • Twilio API    │
│  • SendGrid API  │          │  • E.164 format  │
│  • Thymeleaf     │          │                  │
│    templates     │          │                  │
└─────────┬────────┘          └─────────┬────────┘
          │                             │
          ▼                             ▼
┌──────────────────────────────────────────────────┐
│  External Email/SMS Providers                     │
│  • Gmail SMTP / SendGrid                         │
│  • Twilio                                        │
└──────────────────────────────────────────────────┘
```

### Notification Templates

**Location:** `backend/src/main/resources/templates/email/`

**Available Templates:**
1. `booking-confirmation.html` - Sent immediately after payment
2. `appointment-reminder.html` - Sent 24 hours before
3. `cancellation-confirmation.html` - Sent when cancelled
4. `payment-receipt.html` - Payment confirmation
5. `rescheduling-confirmation.html` - Sent when rescheduled
6. `message-notification.html` - New message alert

**Template Variables:**
- `${patientName}`
- `${psychologistName}`
- `${appointmentDate}`
- `${appointmentTime}`
- `${modality}`
- `${managementLink}`
- `${messageContent}`

**Example Template:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <h1>Appointment Confirmed</h1>
    <p>Dear <span th:text="${patientName}">Patient</span>,</p>
    <p>Your appointment with <span th:text="${psychologistName}">Psychologist</span>
       is confirmed for <span th:text="${appointmentDate}">Date</span>
       at <span th:text="${appointmentTime}">Time</span>.</p>
    <p>
        <a th:href="${managementLink}">Manage Booking</a>
    </p>
</body>
</html>
```

### Retry Logic

```java
public void processPendingNotifications() {
    List<Notification> pending = notificationRepository
        .findByStatusAndScheduledForBefore("PENDING", LocalDateTime.now());

    for (Notification notification : pending) {
        try {
            if (notification.getDeliveryMethod() == DeliveryMethod.EMAIL ||
                notification.getDeliveryMethod() == DeliveryMethod.BOTH) {
                emailService.sendTemplated(/* ... */);
            }

            if (notification.getDeliveryMethod() == DeliveryMethod.SMS ||
                notification.getDeliveryMethod() == DeliveryMethod.BOTH) {
                smsService.sendSms(/* ... */);
            }

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            notification.setRetryCount(notification.getRetryCount() + 1);

            if (notification.getRetryCount() >= 3) {
                notification.setStatus("FAILED");
                notification.setErrorMessage(e.getMessage());
            } else {
                // Retry in 5 minutes
                notification.setScheduledFor(LocalDateTime.now().plusMinutes(5));
            }
        }

        notificationRepository.save(notification);
    }
}
```

---

## Messaging System

### Message Thread Architecture

**Thread ID Format:**
```
Appointment-based: {appointmentId}_{userId1}_{userId2}
User-based:        thread_{userId1}_{userId2}

Where userId1 < userId2 (sorted alphabetically for consistency)
```

**Message Flow:**
```
Psychologist/Client
    │
    ▼
POST /api/messages
{ receiverId, receiverType, subject, content, appointmentId }
    │
    ▼
MessageController (authenticated)
    │
    ▼
Extract senderId from JWT token (SecurityContext)
    │
    ▼
MessageService.sendMessage()
    │
    ├─ Generate threadId
    ├─ Create Message document
    ├─ Save to MongoDB
    │
    ▼
NotificationSchedulerService.sendMessageNotification()
    │
    ▼
Recipient receives email notification
```

### Read Receipts

```java
public Message markAsRead(String messageId, String userId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

    // Only receiver can mark as read
    if (!message.getReceiverId().equals(userId)) {
        throw new UnauthorizedException("Cannot mark other's messages as read");
    }

    message.setIsRead(true);
    message.setReadAt(LocalDateTime.now());

    return messageRepository.save(message);
}
```

---

## Availability & Scheduling System

### Availability Calculation

**Input:**
- Psychologist ID
- Date (e.g., 2025-10-10)
- Duration (e.g., 60 minutes)

**Algorithm:**
```java
public List<LocalDateTime> getAvailableSlots(String psychologistId, LocalDate date, Integer duration) {
    // Step 1: Get day of week (e.g., WEDNESDAY)
    DayOfWeek dayOfWeek = date.getDayOfWeek();

    // Step 2: Get recurring availability for this day
    List<Availability> availabilities = availabilityRepository
        .findByPsychologistIdAndDayOfWeek(psychologistId, dayOfWeek.toString());

    // Step 3: Check if date is in time off
    List<TimeOff> timeOffs = timeOffRepository.findByPsychologistId(psychologistId);
    boolean isTimeOff = timeOffs.stream().anyMatch(timeOff ->
        !date.isBefore(timeOff.getStartDateTime().toLocalDate()) &&
        !date.isAfter(timeOff.getEndDateTime().toLocalDate())
    );

    if (isTimeOff) {
        return Collections.emptyList();
    }

    // Step 4: Get existing bookings for this date
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    List<GuestBooking> bookings = guestBookingRepository
        .findByPsychologistIdAndAppointmentDateTimeBetween(
            psychologistId, startOfDay, endOfDay
        )
        .stream()
        .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED ||
                     b.getBookingStatus() == BookingStatus.PENDING_PAYMENT)
        .collect(Collectors.toList());

    // Step 5: Generate slots
    List<LocalDateTime> availableSlots = new ArrayList<>();

    for (Availability availability : availabilities) {
        LocalTime startTime = LocalTime.parse(availability.getStartTime());
        LocalTime endTime = LocalTime.parse(availability.getEndTime());

        LocalDateTime current = date.atTime(startTime);
        LocalDateTime end = date.atTime(endTime);

        while (current.plusMinutes(duration).isBefore(end) ||
               current.plusMinutes(duration).equals(end)) {

            // Check if slot conflicts with existing booking
            LocalDateTime slotEnd = current.plusMinutes(duration);
            boolean conflicts = bookings.stream().anyMatch(booking -> {
                LocalDateTime bookingStart = booking.getAppointmentDateTime();
                LocalDateTime bookingEnd = bookingStart.plusMinutes(booking.getDurationMinutes());

                return !(slotEnd.isBefore(bookingStart) ||
                         current.isAfter(bookingEnd) ||
                         current.equals(bookingEnd));
            });

            // Only add if no conflict and in the future
            if (!conflicts && current.isAfter(LocalDateTime.now())) {
                availableSlots.add(current);
            }

            // Move to next slot (increment by duration)
            current = current.plusMinutes(duration);
        }
    }

    return availableSlots;
}
```

**Example:**
```
Date: 2025-10-10 (Wednesday)
Psychologist Availability: Wednesday 9:00 AM - 5:00 PM
Existing Bookings:
  - 10:00 AM - 11:00 AM
  - 2:00 PM - 3:00 PM
Duration: 60 minutes

Available Slots:
  - 9:00 AM
  - 11:00 AM
  - 12:00 PM
  - 1:00 PM
  - 3:00 PM
  - 4:00 PM
```

### Time Off Handling

**Effective Dates:**
- `effectiveFrom` - Start date for availability (optional)
- `effectiveUntil` - End date for availability (optional)
- Used for temporary availability changes (e.g., summer schedule)

**Time Off:**
- `startDateTime` - Start of time off period
- `endDateTime` - End of time off period
- `reason` - Description (optional)

**Priority:**
1. Time off periods override recurring availability
2. Effective date ranges limit recurring availability

---

## Data Flow Diagrams

### Guest Booking Flow

```
┌────────┐
│ Patient│
└───┬────┘
    │
    │ 1. Browse psychologists
    │    GET /api/public/psychologists
    ▼
┌────────────────────────────┐
│ PsychologistListPage       │
└───┬────────────────────────┘
    │
    │ 2. Select psychologist
    │    Navigate to /book/:id
    ▼
┌────────────────────────────┐
│ BookingPage                │
│ • Step 1: Session type     │
│ • Step 2: Date & time      │
│   GET /api/public/psychologists/:id/availability
│ • Step 3: Personal info    │
│ • Step 4: Review           │
└───┬────────────────────────┘
    │
    │ 3. Submit booking
    │    POST /api/public/bookings
    ▼
┌─────────────────────────────────────────┐
│ GuestBookingService                      │
│ 1. Validate availability                │
│ 2. Create booking (PENDING_PAYMENT)     │
│ 3. Generate confirmationToken (UUID)    │
│ 4. Create Stripe checkout session       │
│ 5. Save booking with Stripe session ID  │
└───┬─────────────────────────────────────┘
    │
    │ 4. Return checkout URL
    ▼
┌────────────────────────────┐
│ Redirect to Stripe         │
└───┬────────────────────────┘
    │
    │ 5. Complete payment
    ▼
┌────────────────────────────┐
│ Stripe processes payment   │
└───┬────────────────────────┘
    │
    │ 6. Webhook: checkout.session.completed
    ▼
┌─────────────────────────────────────────┐
│ StripeWebhookController                 │
│ 1. Verify signature                     │
│ 2. Find booking by session ID           │
│ 3. Call handlePaymentSuccess()          │
└───┬─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│ GuestBookingService.handlePaymentSuccess│
│ 1. Update booking (CONFIRMED)           │
│ 2. Update payment (COMPLETED)           │
│ 3. Send confirmation email              │
│ 4. Send confirmation SMS                │
│ 5. Schedule 24-hour reminder            │
└───┬─────────────────────────────────────┘
    │
    │ 7. Redirect to success page
    ▼
┌────────────────────────────┐
│ BookingSuccessPage         │
└────────────────────────────┘
```

### Psychologist Dashboard Flow

```
┌──────────────┐
│ Psychologist │
└───┬──────────┘
    │
    │ 1. Navigate to /psychologist/login
    ▼
┌────────────────────────────┐
│ LoginPage                  │
│ • Email + password form    │
└───┬────────────────────────┘
    │
    │ 2. Submit credentials
    │    POST /api/auth/login
    ▼
┌─────────────────────────────────────────┐
│ AuthService                             │
│ 1. Load psychologist by email           │
│ 2. Verify password (BCrypt)             │
│ 3. Generate JWT token                   │
│ 4. Return token + user info             │
└───┬─────────────────────────────────────┘
    │
    │ 3. Store token in localStorage + Redux
    │    Navigate to /psychologist/dashboard
    ▼
┌────────────────────────────┐
│ DashboardPage              │
└───┬────────────────────────┘
    │
    │ 4. Load dashboard
    │    GET /api/psychologist/dashboard
    │    Authorization: Bearer <token>
    ▼
┌─────────────────────────────────────────┐
│ PsychologistService.getDashboard()      │
│ 1. Extract psychologist ID from JWT     │
│ 2. Get today's bookings                 │
│ 3. Get upcoming bookings (next 7 days)  │
│ 4. Calculate statistics:                │
│    • Total sessions                     │
│    • Pending bookings                   │
│    • Completed this week/month          │
│    • Cancelled/no-shows this month      │
│    • Unread messages                    │
│ 5. Build DashboardDTO                   │
└───┬─────────────────────────────────────┘
    │
    │ 5. Return dashboard data
    ▼
┌────────────────────────────┐
│ DashboardPage renders:     │
│ • Stats cards              │
│ • Today's appointments     │
│ • Upcoming appointments    │
│ • Quick actions            │
└────────────────────────────┘
```

---

## Deployment Architecture

### Recommended Production Setup (AWS)

```
┌────────────────────────────────────────────────────────────────┐
│  Route 53 (DNS)                                                 │
│  groundandgrow.com.au → CloudFront → ALB                       │
└────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────────────────────────────┐
│  CloudFront (CDN)                                               │
│  • Cache static assets                                         │
│  • SSL/TLS termination                                         │
│  • DDoS protection                                             │
└────────────────┬───────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌──────────────┐   ┌──────────────┐
│ S3 Bucket    │   │ Application  │
│ (Frontend)   │   │ Load Balancer│
│ React static │   │ (ALB)        │
│ files        │   └──────┬───────┘
└──────────────┘          │
                          ▼
                ┌──────────────────┐
                │ EC2 Auto Scaling │
                │ Group            │
                │ • Min: 2         │
                │ • Max: 10        │
                └──────┬───────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  EC2 (1)    │ │  EC2 (2)    │ │  EC2 (n)    │
│  Spring Boot│ │  Spring Boot│ │  Spring Boot│
│  Container  │ │  Container  │ │  Container  │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │               │
       └───────────────┼───────────────┘
                       │
                       ▼
        ┌──────────────────────────┐
        │  MongoDB Atlas           │
        │  • Primary + 2 Replicas  │
        │  • Auto-backup           │
        │  • Point-in-time recovery│
        └──────────────────────────┘
```

### Environment Configuration

**Development:**
- MongoDB: Local instance (localhost:27017)
- Email: Mailtrap or Gmail test account
- SMS: Twilio test credentials
- Stripe: Test keys
- Frontend: Vite dev server (localhost:5173)
- Backend: Spring Boot dev mode (localhost:8080)

**Staging:**
- MongoDB: MongoDB Atlas (shared cluster)
- Email: SendGrid sandbox
- SMS: Twilio trial account
- Stripe: Test keys
- Frontend: S3 + CloudFront (staging subdomain)
- Backend: Single EC2 instance

**Production:**
- MongoDB: MongoDB Atlas (dedicated cluster, M10+)
- Email: SendGrid production
- SMS: Twilio production
- Stripe: Live keys
- Frontend: S3 + CloudFront (production domain)
- Backend: EC2 Auto Scaling Group (min 2 instances)

---

## Security Architecture

### Security Layers

```
┌────────────────────────────────────────────────────────────┐
│  Layer 1: Network Security                                  │
│  • VPC with private/public subnets                         │
│  • Security groups (firewall rules)                        │
│  • HTTPS only (TLS 1.2+)                                   │
└────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Layer 2: Application Security                              │
│  • CORS configuration                                      │
│  • JWT authentication                                      │
│  • Role-based access control                              │
│  • Input validation (@Valid, JSR-303)                     │
│  • SQL/NoSQL injection prevention (Spring Data)           │
└────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Layer 3: Data Security                                     │
│  • Password hashing (BCrypt)                               │
│  • JWT signing (HMAC-SHA512)                               │
│  • Database encryption at rest (MongoDB)                   │
│  • TLS in transit (MongoDB Atlas)                          │
│  • PCI DSS compliance (Stripe)                             │
└────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Layer 4: Compliance & Privacy                              │
│  • Australian Privacy Act compliance                       │
│  • HIPAA considerations (healthcare data)                  │
│  • Data retention policies                                 │
│  • Right to access/delete data (GDPR-inspired)             │
└────────────────────────────────────────────────────────────┘
```

### Security Best Practices Implemented

1. **Password Security:**
   - BCrypt hashing (strength 12 rounds)
   - Minimum password length enforced
   - No password in DTOs or logs

2. **JWT Security:**
   - HS512 signing algorithm
   - 24-hour expiration
   - Secret key stored in environment variables (min 256 bits)
   - Token validation on every request

3. **API Security:**
   - CORS restricted to known origins
   - Stripe webhook signature verification
   - Rate limiting (recommended for production)

4. **Data Privacy:**
   - Psychologist notes separate from patient-visible notes
   - Soft deletes to preserve audit trail
   - No sensitive data in logs

5. **Payment Security:**
   - PCI DSS compliance via Stripe
   - No card data stored on servers
   - Webhook signature verification

---

## Monitoring & Logging

### Logging Strategy

**Log Levels:**
- `ERROR` - Critical errors requiring immediate attention
- `WARN` - Warning conditions (e.g., retry attempts)
- `INFO` - Informational messages (e.g., booking created)
- `DEBUG` - Detailed debugging information (development only)

**Logging Configuration (`application.yml`):**
```yaml
logging:
  level:
    root: INFO
    com.groundandgrow: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
  file:
    name: logs/groundandgrow.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Monitoring Metrics (Recommended)

**Application Metrics:**
- API response times (p50, p95, p99)
- Error rates by endpoint
- Active users (JWT token usage)
- Booking conversion rate
- Payment success rate

**Infrastructure Metrics:**
- CPU utilization
- Memory usage
- Disk I/O
- Network throughput
- MongoDB query performance

**Business Metrics:**
- Bookings per day/week/month
- Revenue
- Cancellation rate
- Average session value
- Psychologist utilization rate

**Tools:**
- **Application:** Spring Boot Actuator, Micrometer
- **Infrastructure:** AWS CloudWatch, DataDog, New Relic
- **Errors:** Sentry, LogRocket
- **Uptime:** UptimeRobot, Pingdom

---

## Performance Considerations

### Backend Performance

**Database Optimization:**
- Indexes on frequently queried fields
- Compound indexes for common query patterns
- Projection queries (fetch only needed fields)
- Connection pooling (MongoDB driver default)

**Caching Strategy (Future):**
```java
@Cacheable("psychologists")
public List<PsychologistDTO> getAllActivePsychologists() {
    // Cache for 1 hour
}

@Cacheable("availability")
public List<LocalDateTime> getAvailableSlots(String psychologistId, LocalDate date) {
    // Cache for 5 minutes
}
```

**API Response Times (Target):**
- Authentication: < 500ms
- Psychologist list: < 200ms
- Availability calculation: < 500ms
- Booking creation: < 1000ms (includes Stripe API call)
- Dashboard: < 800ms

### Frontend Performance

**Bundle Optimization:**
- Code splitting with React.lazy()
- Tree shaking (Vite automatic)
- Minification and compression

**Loading States:**
- Skeleton screens for better perceived performance
- Optimistic UI updates
- Debounced API calls (search, autocomplete)

**Caching:**
- Service worker for offline support (future)
- LocalStorage for frequently accessed data
- Redux persist for state persistence

---

## Scalability Strategies

### Horizontal Scaling

**Backend:**
- Stateless API design (no server-side sessions)
- Load balancer distributes traffic across multiple instances
- Auto-scaling based on CPU/memory metrics

**Database:**
- MongoDB replica set (primary + secondaries)
- Read preference: secondary for read-heavy operations
- Sharding for very large collections (future)

**Frontend:**
- CDN distribution (CloudFront)
- Static asset caching
- Lazy loading of routes and components

### Vertical Scaling

**When to scale up vs. out:**
- Scale up: Database (MongoDB Atlas tier upgrade)
- Scale out: Backend API servers (add more EC2 instances)

### Microservices Migration (Future)

**Candidates for extraction:**
1. Notification Service (Email/SMS)
2. Payment Service (Stripe integration)
3. Availability Service (complex calculation logic)

---

## Disaster Recovery

### Backup Strategy

**MongoDB:**
- Automated daily backups (MongoDB Atlas)
- Point-in-time recovery (last 72 hours)
- Cross-region replication

**Application Code:**
- Git version control
- GitHub repository (private)
- Regular commits and tags

**Environment Configuration:**
- Environment variables documented in .env.example
- Secrets stored in AWS Secrets Manager or similar

### Recovery Procedures

**Database Failure:**
1. MongoDB Atlas automatic failover (< 30 seconds)
2. If primary fails, secondary promoted automatically
3. Application continues with minimal disruption

**Application Failure:**
1. Auto-scaling group launches new instances
2. Health checks remove failed instances
3. Load balancer routes traffic to healthy instances

**Complete System Failure:**
1. Restore MongoDB from latest backup
2. Deploy application from Git tag
3. Configure environment variables
4. Verify Stripe webhooks
5. Test critical flows (booking, login)

**RTO (Recovery Time Objective):** < 4 hours
**RPO (Recovery Point Objective):** < 24 hours

---

## Development Workflow

### Git Workflow

**Branch Strategy:**
```
main
  ├── develop
  │   ├── feature/agent-1-authentication
  │   ├── feature/agent-2-booking
  │   ├── feature/agent-3-notifications
  │   ├── feature/agent-4-psychologist-portal
  │   └── feature/agent-5-frontend
  └── hotfix/production-bug-fix
```

**Commit Convention:**
```
type(scope): subject

Types: feat, fix, docs, style, refactor, test, chore
Scope: backend, frontend, database, ci

Examples:
feat(backend): Add JWT authentication
fix(frontend): Resolve booking form validation issue
docs(readme): Update setup instructions
```

### Code Review Process

1. Developer creates pull request
2. Automated checks run (tests, linting)
3. Code review by peer
4. Approval required before merge
5. Merge to develop branch
6. Deploy to staging for testing
7. Merge to main for production

---

## Testing Strategy

### Backend Testing

**Unit Tests:**
```java
@Test
public void testJwtTokenGeneration() {
    String userId = "user123";
    String token = jwtTokenProvider.generateToken(userId);
    assertNotNull(token);
    assertTrue(jwtTokenProvider.validateToken(token));
}
```

**Integration Tests:**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }
}
```

**Test Coverage Target:** > 80%

### Frontend Testing

**Component Tests:**
```typescript
import { render, screen } from '@testing-library/react';
import { LoginPage } from './LoginPage';

test('renders login form', () => {
  render(<LoginPage />);
  expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
});
```

**Integration Tests:**
```typescript
test('successful login redirects to dashboard', async () => {
  render(<LoginPage />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: { value: 'test@example.com' }
  });
  fireEvent.change(screen.getByLabelText(/password/i), {
    target: { value: 'password123' }
  });

  fireEvent.click(screen.getByRole('button', { name: /login/i }));

  await waitFor(() => {
    expect(window.location.pathname).toBe('/psychologist/dashboard');
  });
});
```

### End-to-End Testing (Future)

**Tools:** Cypress, Playwright

**Test Scenarios:**
1. Complete booking flow (guest)
2. Psychologist login and appointment management
3. Cancellation and refund
4. Messaging between psychologist and client

---

## CI/CD Pipeline

### Recommended Pipeline (GitHub Actions)

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: cd backend && mvn test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: cd frontend && npm ci
      - name: Run tests
        run: cd frontend && npm test

  deploy-staging:
    needs: [backend-test, frontend-test]
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to staging
        run: |
          # Deploy backend to staging EC2
          # Deploy frontend to S3 staging bucket
          # Invalidate CloudFront cache

  deploy-production:
    needs: [backend-test, frontend-test]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to production
        run: |
          # Deploy backend to production EC2 Auto Scaling Group
          # Deploy frontend to S3 production bucket
          # Invalidate CloudFront cache
```

---

## Troubleshooting Guide

### Common Issues

#### 1. JWT Token Expired
**Symptom:** 401 Unauthorized on protected endpoints
**Cause:** Token expired (24 hours)
**Solution:** Refresh token via `/api/auth/refresh` or re-login

#### 2. Stripe Webhook Not Received
**Symptom:** Booking stuck in PENDING_PAYMENT
**Cause:** Webhook not configured or signature mismatch
**Solution:**
- Verify webhook endpoint in Stripe dashboard
- Check webhook secret matches environment variable
- Use Stripe CLI to forward webhooks in development

#### 3. Notification Not Sent
**Symptom:** No email/SMS received
**Cause:** Notification stuck in FAILED status
**Solution:**
- Check notification status in MongoDB
- Review error message in notification document
- Verify email/SMS credentials in environment variables
- Check retry count (max 3 attempts)

#### 4. Availability Slots Not Showing
**Symptom:** Empty availability list
**Cause:** No recurring availability or all slots booked
**Solution:**
- Verify psychologist has availability configured for that day
- Check if date is in time off period
- Ensure requested time is in the future

#### 5. MongoDB Connection Failed
**Symptom:** Application fails to start
**Cause:** MongoDB not running or incorrect connection string
**Solution:**
- Verify MongoDB is running (local or Atlas)
- Check `MONGODB_URI` environment variable
- Ensure network access allowed in Atlas

---

## Appendices

### Appendix A: Environment Variables Reference

**Backend (.env):**
```bash
# MongoDB
MONGODB_URI=mongodb://localhost:27017/groundandgrow

# JWT
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# Stripe
STRIPE_API_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_SUCCESS_URL=${FRONTEND_URL}/booking/success?session_id={CHECKOUT_SESSION_ID}
STRIPE_CANCEL_URL=${FRONTEND_URL}/booking/cancelled

# Email - JavaMail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@groundandgrow.com.au
MAIL_PASSWORD=app-password
EMAIL_ENABLED=true

# Email - SendGrid
SENDGRID_ENABLED=false
SENDGRID_API_KEY=SG...

# SMS - Twilio
TWILIO_ENABLED=true
TWILIO_ACCOUNT_SID=AC...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=+61412345678
SMS_ENABLED=true

# Application
FRONTEND_URL=http://localhost:5173
APP_CANCELLATION_HOURS_NOTICE=24
APP_NOTIFICATION_EMAIL_FROM=noreply@groundandgrow.com.au
APP_NOTIFICATION_EMAIL_FROM_NAME=Ground & Grow Psychology
CORS_ORIGINS=http://localhost:5173,http://localhost:3000

# Admin
ADMIN_USERNAME=admin
ADMIN_PASSWORD=changeme
```

**Frontend (.env):**
```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Ground & Grow Psychology
VITE_STRIPE_PUBLIC_KEY=pk_test_...
```

### Appendix B: API Endpoint Reference

See [CHANGELOG.md](CHANGELOG.md) for complete API endpoint list.

### Appendix C: Database Schema ERD

```
┌─────────────────┐       ┌──────────────────┐       ┌───────────────┐
│  psychologists  │       │  session_types   │       │  clients      │
├─────────────────┤       ├──────────────────┤       ├───────────────┤
│ _id             │       │ _id              │       │ _id           │
│ firstName       │       │ name             │       │ firstName     │
│ lastName        │       │ description      │       │ lastName      │
│ email (unique)  │       │ durationMinutes  │       │ email (unique)│
│ password        │       │ price            │       │ phone         │
│ role            │       │ modality         │       │ ...           │
│ ...             │       │ isActive         │       └───────────────┘
└────────┬────────┘       └──────────────────┘
         │
         │ 1:N
         │
         ▼
┌─────────────────┐       ┌──────────────────┐
│  availability   │       │  time_off        │
├─────────────────┤       ├──────────────────┤
│ _id             │       │ _id              │
│ psychologistId ─┼───────│ psychologistId   │
│ dayOfWeek       │       │ startDateTime    │
│ startTime       │       │ endDateTime      │
│ endTime         │       │ reason           │
│ isRecurring     │       └──────────────────┘
│ effectiveFrom   │
│ effectiveUntil  │
└─────────────────┘
         │
         │ 1:N
         │
         ▼
┌──────────────────┐       ┌──────────────────┐
│  guest_bookings  │       │  messages        │
├──────────────────┤       ├──────────────────┤
│ _id              │       │ _id              │
│ psychologistId ──┼───┐   │ senderId         │
│ sessionTypeId    │   │   │ receiverId       │
│ appointmentDate  │   │   │ senderType       │
│ confirmationToken│   │   │ receiverType     │
│ stripeCheckoutId │   │   │ subject          │
│ bookingStatus    │   │   │ content          │
│ paymentStatus    │   │   │ appointmentId    │
│ ...              │   │   │ threadId         │
└──────────────────┘   │   │ isRead           │
                       │   │ ...              │
                       │   └──────────────────┘
                       │
                       │
                       │   ┌──────────────────┐
                       │   │  notifications   │
                       │   ├──────────────────┤
                       │   │ _id              │
                       └───│ guestBookingId   │
                           │ recipientId      │
                           │ notificationType │
                           │ deliveryMethod   │
                           │ scheduledFor     │
                           │ status           │
                           │ ...              │
                           └──────────────────┘
```

### Appendix D: Technology Versions

| Technology | Version | Latest Stable | Notes |
|-----------|---------|---------------|-------|
| Java | 17 | 21 | LTS version, stable |
| Spring Boot | 3.2.0 | 3.2.x | Latest 3.x series |
| React | 18.2.0 | 18.2.x | Latest stable |
| TypeScript | 5.2.2 | 5.3.x | Consider upgrade |
| MongoDB | 6.0+ | 7.0 | Atlas supports 6.0+ |
| Stripe Java SDK | 24.16.0 | 25.x | Consider upgrade |
| Twilio | 9.14.1 | 10.x | Breaking changes in 10.x |

---

## Glossary

- **BCrypt:** Password hashing algorithm
- **CORS:** Cross-Origin Resource Sharing
- **DTO:** Data Transfer Object
- **E.164:** International phone number format
- **JWT:** JSON Web Token
- **JJWT:** Java JWT library
- **PCI DSS:** Payment Card Industry Data Security Standard
- **REST:** Representational State Transfer
- **SPA:** Single Page Application
- **TLS:** Transport Layer Security
- **UUID:** Universally Unique Identifier

---

**Document Version:** 1.0
**Last Updated:** 2025-10-05
**Maintained By:** Development Team

**For questions or updates to this document, please contact the development team or create an issue in the project repository.**
