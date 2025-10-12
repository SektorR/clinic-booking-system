# Claude Agent Instructions - Ground & Grow Psychology Development

This document provides detailed instructions for 5 Claude agents to work in parallel to build the Ground & Grow Psychology application.

---

## Prerequisites

Before starting, ensure:
1. You have read `.claude/context.md` and `FEATURES.md`
2. MongoDB is running (local or Atlas)
3. You have Stripe test account credentials
4. You have email service credentials (Gmail or SendGrid)
5. (Optional) Twilio credentials for SMS

---

## Agent 1: Backend Core & Authentication

### Responsibility
Build the core Spring Boot backend infrastructure, database models, and psychologist authentication system.

### Tasks

#### 1.1 Complete Database Models
Create all MongoDB entity classes in `backend/src/main/java/com/groundandgrow/model/`:

- **Client.java** - Registered client model (for future feature)
  ```java
  @Document(collection = "clients")
  - id, firstName, lastName, email, phone
  - dateOfBirth, address
  - emergencyContact (name, phone)
  - createdAt, updatedAt, isActive
  ```

- **Psychologist.java** - Psychologist/practitioner model
  ```java
  @Document(collection = "psychologists")
  - id, firstName, lastName, email, phone
  - specialization, registrationNumber, bio
  - password (hashed), role (enum: PSYCHOLOGIST, ADMIN)
  - createdAt, updatedAt, isActive
  ```

- **SessionType.java** - Service types offered
  ```java
  @Document(collection = "session_types")
  - id, name, description
  - durationMinutes, price
  - modality (enum: ONLINE, IN_PERSON, PHONE)
  - isActive
  ```

- **Availability.java** - Psychologist availability schedule
  ```java
  @Document(collection = "availability")
  - id, psychologistId
  - dayOfWeek (enum: MONDAY-SUNDAY)
  - startTime, endTime
  - isRecurring, effectiveFrom, effectiveUntil
  ```

- **TimeOff.java** - Exceptions/time off
  ```java
  @Document(collection = "time_off")
  - id, psychologistId
  - startDateTime, endDateTime
  - reason, createdAt
  ```

**Models already created (no need to recreate):**
- GuestBooking.java ✓
- Message.java ✓
- Notification.java ✓

#### 1.2 Create Repositories
Create repository interfaces in `backend/src/main/java/com/groundandgrow/repository/`:

```java
@Repository
public interface PsychologistRepository extends MongoRepository<Psychologist, String> {
    Optional<Psychologist> findByEmail(String email);
    List<Psychologist> findByIsActive(Boolean isActive);
}

@Repository
public interface SessionTypeRepository extends MongoRepository<SessionType, String> {
    List<SessionType> findByModalityAndIsActive(String modality, Boolean isActive);
}

@Repository
public interface AvailabilityRepository extends MongoRepository<Availability, String> {
    List<Availability> findByPsychologistId(String psychologistId);
    List<Availability> findByPsychologistIdAndDayOfWeek(String psychologistId, String dayOfWeek);
}

@Repository
public interface TimeOffRepository extends MongoRepository<TimeOff, String> {
    List<TimeOff> findByPsychologistId(String psychologistId);
}

@Repository
public interface GuestBookingRepository extends MongoRepository<GuestBooking, String> {
    Optional<GuestBooking> findByConfirmationToken(String token);
    List<GuestBooking> findByEmail(String email);
    List<GuestBooking> findByPsychologistId(String psychologistId);
}

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByThreadId(String threadId);
    List<Message> findByReceiverIdAndIsRead(String receiverId, Boolean isRead);
}

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByStatusAndScheduledForBefore(String status, LocalDateTime time);
}
```

#### 1.3 JWT Authentication System
Create JWT security infrastructure in `backend/src/main/java/com/groundandgrow/security/`:

- **JwtTokenProvider.java** - Generate and validate JWT tokens
- **JwtAuthenticationFilter.java** - Filter for token validation
- **SecurityConfig.java** - Spring Security configuration
  - Allow public access to: `/api/public/**`, `/api/webhooks/**`, `/swagger-ui/**`
  - Require authentication for: `/api/psychologist/**`, `/api/admin/**`

- **UserDetailsServiceImpl.java** - Load psychologist details for authentication

#### 1.4 DTOs (Data Transfer Objects)
Create DTOs in `backend/src/main/java/com/groundandgrow/dto/`:

- **LoginRequest.java** - email, password
- **LoginResponse.java** - token, psychologistId, email, name
- **PsychologistDTO.java** - Public psychologist info (no password)
- **SessionTypeDTO.java** - Session type info
- **AvailabilityDTO.java** - Availability info

#### 1.5 Authentication Controller
Create `AuthController.java` in `backend/src/main/java/com/groundandgrow/controller/`:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);

    @PostMapping("/register") // For admin to create psychologist accounts
    public ResponseEntity<PsychologistDTO> register(@RequestBody RegisterRequest request);

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String token);
}
```

#### 1.6 Testing
- Write unit tests for JwtTokenProvider
- Write integration tests for AuthController
- Test MongoDB connection and repository methods

---

## Agent 2: Guest Booking & Payment System

### Responsibility
Build the guest booking system with Stripe payment integration and public-facing APIs.

### Tasks

#### 2.1 Public API Controllers
Create controllers in `backend/src/main/java/com/groundandgrow/controller/`:

**PublicPsychologistController.java**
```java
@RestController
@RequestMapping("/api/public/psychologists")
public class PublicPsychologistController {

    @GetMapping
    public ResponseEntity<List<PsychologistDTO>> getAllActivePsychologists();

    @GetMapping("/{id}")
    public ResponseEntity<PsychologistDTO> getPsychologistById(@PathVariable String id);

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityDTO> getAvailability(
        @PathVariable String id,
        @RequestParam LocalDate date
    );

    @GetMapping("/session-types")
    public ResponseEntity<List<SessionTypeDTO>> getAllSessionTypes();
}
```

**GuestBookingController.java**
```java
@RestController
@RequestMapping("/api/public/bookings")
public class GuestBookingController {

    @PostMapping
    public ResponseEntity<CheckoutSessionResponse> createBooking(
        @RequestBody GuestBookingRequest request
    );
    // Returns Stripe checkout URL

    @GetMapping("/{token}")
    public ResponseEntity<GuestBookingDTO> getBookingByToken(@PathVariable String token);

    @PutMapping("/{token}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(@PathVariable String token);

    @PutMapping("/{token}/reschedule")
    public ResponseEntity<GuestBookingDTO> rescheduleBooking(
        @PathVariable String token,
        @RequestBody RescheduleRequest request
    );
}
```

#### 2.2 Booking Service
Create `GuestBookingService.java` in `backend/src/main/java/com/groundandgrow/service/`:

```java
@Service
public class GuestBookingService {

    // Inject: GuestBookingRepository, StripeService, EmailService, SmsService

    public CheckoutSessionResponse createBooking(GuestBookingRequest request) {
        // 1. Validate availability
        // 2. Create guest booking (status: PENDING_PAYMENT)
        // 3. Generate confirmation token (UUID)
        // 4. Create Stripe checkout session
        // 5. Save booking with Stripe session ID
        // 6. Return checkout URL
    }

    public void handlePaymentSuccess(String sessionId) {
        // Called by Stripe webhook
        // 1. Find booking by Stripe session ID
        // 2. Update payment status to COMPLETED
        // 3. Update booking status to CONFIRMED
        // 4. Send confirmation email
        // 5. Send confirmation SMS
        // 6. Schedule reminder notification
    }

    public void handlePaymentFailure(String sessionId) {
        // 1. Find booking
        // 2. Update status to FAILED
        // 3. Send failure notification
    }

    public CancellationResponse cancelBooking(String token) {
        // 1. Find booking by token
        // 2. Validate cancellation policy (24 hours notice)
        // 3. Process refund via Stripe
        // 4. Update booking status to CANCELLED
        // 5. Send cancellation email/SMS
    }

    public GuestBooking rescheduleBooking(String token, LocalDateTime newDateTime) {
        // 1. Find booking
        // 2. Validate new availability
        // 3. Update booking
        // 4. Send rescheduling confirmation
    }
}
```

#### 2.3 Stripe Webhook Handler
Create `StripeWebhookController.java`:

```java
@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String signature
    ) {
        // 1. Verify webhook signature
        // 2. Parse event
        // 3. Handle different event types:
        //    - checkout.session.completed
        //    - payment_intent.succeeded
        //    - payment_intent.payment_failed
        //    - charge.refunded
        // 4. Call appropriate service methods
    }
}
```

#### 2.4 Availability Service
Create `AvailabilityService.java`:

```java
@Service
public class AvailabilityService {

    public List<LocalDateTime> getAvailableSlots(
        String psychologistId,
        LocalDate date,
        Integer durationMinutes
    ) {
        // 1. Get psychologist's recurring availability for day
        // 2. Get existing bookings for that day
        // 3. Get time off exceptions
        // 4. Calculate available time slots
        // 5. Return list of available start times
    }

    public boolean isSlotAvailable(
        String psychologistId,
        LocalDateTime startTime,
        Integer durationMinutes
    ) {
        // Check if specific slot is available
    }
}
```

#### 2.5 DTOs
Create request/response DTOs in `backend/src/main/java/com/groundandgrow/dto/`:

- **GuestBookingRequest.java**
- **GuestBookingDTO.java**
- **CheckoutSessionResponse.java**
- **CancellationResponse.java**
- **RescheduleRequest.java**

#### 2.6 Testing
- Test booking creation flow
- Test Stripe webhook handling (use Stripe CLI)
- Test availability calculation
- Test cancellation and refund logic

---

## Agent 3: Notification & Communication System

### Responsibility
Build the notification scheduling system, email/SMS services, and messaging functionality.

### Tasks

#### 3.1 Notification Scheduler Service
Create `NotificationSchedulerService.java` in `backend/src/main/java/com/groundandgrow/service/`:

```java
@Service
public class NotificationSchedulerService {

    // Inject: NotificationRepository, EmailService, SmsService

    @Scheduled(fixedDelay = 60000) // Run every minute
    public void processPendingNotifications() {
        // 1. Query notifications with status=PENDING and scheduledFor <= NOW
        // 2. For each notification:
        //    - Send via appropriate method (email/sms)
        //    - Update status to SENT or FAILED
        //    - Track external IDs
        //    - Handle retries
    }

    public Notification scheduleNotification(NotificationRequest request) {
        // Create and save notification for future sending
    }

    public void scheduleAppointmentReminder(GuestBooking booking) {
        // Schedule reminder for 24 hours before appointment
        LocalDateTime reminderTime = booking.getAppointmentDateTime().minusHours(24);
        // Create notification scheduled for reminderTime
    }
}
```

#### 3.2 Email Template System
Create Thymeleaf email templates in `backend/src/main/resources/templates/email/`:

**booking-confirmation.html**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Booking Confirmation</title>
</head>
<body>
    <h1>Booking Confirmation</h1>
    <p>Dear <span th:text="${patientName}">Patient</span>,</p>

    <p>Your appointment has been confirmed!</p>

    <div style="background: #f5f5f5; padding: 20px; margin: 20px 0;">
        <h2>Appointment Details</h2>
        <p><strong>Psychologist:</strong> <span th:text="${psychologistName}">Dr. Smith</span></p>
        <p><strong>Date:</strong> <span th:text="${appointmentDate}">Jan 15, 2025</span></p>
        <p><strong>Time:</strong> <span th:text="${appointmentTime}">2:00 PM</span></p>
        <p><strong>Type:</strong> <span th:text="${modality}">Online</span></p>
    </div>

    <p>
        <a th:href="${managementLink}" style="background: #0ea5e9; color: white; padding: 10px 20px; text-decoration: none;">
            Manage Booking
        </a>
    </p>

    <p>If you need to cancel or reschedule, please click the link above.</p>

    <p>Best regards,<br>Ground & Grow Psychology Team</p>
</body>
</html>
```

Create similar templates for:
- **appointment-reminder.html**
- **cancellation-confirmation.html**
- **payment-receipt.html**
- **rescheduling-confirmation.html**
- **message-notification.html**

#### 3.3 Messaging Service
Create `MessageService.java`:

```java
@Service
public class MessageService {

    public Message sendMessage(MessageRequest request) {
        // 1. Create message
        // 2. Save to database
        // 3. Notify recipient via email
        // 4. Return message
    }

    public List<Message> getThreadMessages(String threadId) {
        // Get all messages in a conversation thread
    }

    public Message markAsRead(String messageId) {
        // Update message read status
    }

    public List<Message> getUnreadMessages(String userId) {
        // Get unread messages for user
    }

    public String createThread(String senderId, String receiverId, String appointmentId) {
        // Generate thread ID for new conversation
        // Format: "{appointmentId}_{senderId}_{receiverId}"
    }
}
```

#### 3.4 Messaging Controller
Create `MessageController.java`:

```java
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageRequest request);

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<MessageDTO>> getThreadMessages(@PathVariable String threadId);

    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages();

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable String id);
}
```

#### 3.5 Notification Controller (Internal)
Create `NotificationController.java`:

```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping("/send")
    public ResponseEntity<NotificationDTO> sendNotification(@RequestBody NotificationRequest request);

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationStatus(@PathVariable String id);

    @GetMapping("/guest-booking/{bookingId}")
    public ResponseEntity<List<NotificationDTO>> getBookingNotifications(@PathVariable String bookingId);
}
```

#### 3.6 Enable Scheduling
Update `Ground & Grow PsychologyApplication.java`:

```java
@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling  // Add this annotation
public class Ground & Grow PsychologyApplication {
    // ...
}
```

#### 3.7 Testing
- Test email sending (use Mailtrap or similar)
- Test SMS sending (use Twilio test credentials)
- Test notification scheduling
- Test message creation and retrieval
- Verify HTML email rendering

---

## Agent 4: Psychologist Portal Backend

### Responsibility
Build authenticated APIs for psychologists to manage their schedule, appointments, and clients.

### Tasks

#### 4.1 Psychologist Controller
Create `PsychologistController.java`:

```java
@RestController
@RequestMapping("/api/psychologist")
@PreAuthorize("hasRole('PSYCHOLOGIST')")
public class PsychologistController {

    @GetMapping("/profile")
    public ResponseEntity<PsychologistDTO> getProfile();

    @PutMapping("/profile")
    public ResponseEntity<PsychologistDTO> updateProfile(@RequestBody UpdateProfileRequest request);

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard();
    // Returns: today's appointments, upcoming appointments, stats

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) String status
    );

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentDetails(@PathVariable String id);

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
        @PathVariable String id,
        @RequestBody UpdateStatusRequest request
    );
    // Statuses: COMPLETED, NO_SHOW, etc.

    @PostMapping("/appointments/{id}/notes")
    public ResponseEntity<AppointmentDTO> addNotes(
        @PathVariable String id,
        @RequestBody NotesRequest request
    );
}
```

#### 4.2 Availability Management Controller
Create `AvailabilityController.java`:

```java
@RestController
@RequestMapping("/api/psychologist/availability")
@PreAuthorize("hasRole('PSYCHOLOGIST')")
public class AvailabilityController {

    @GetMapping
    public ResponseEntity<List<AvailabilityDTO>> getMyAvailability();

    @PostMapping
    public ResponseEntity<AvailabilityDTO> addAvailability(@RequestBody AvailabilityRequest request);

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityDTO> updateAvailability(
        @PathVariable String id,
        @RequestBody AvailabilityRequest request
    );

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable String id);

    @GetMapping("/time-off")
    public ResponseEntity<List<TimeOffDTO>> getTimeOff();

    @PostMapping("/time-off")
    public ResponseEntity<TimeOffDTO> addTimeOff(@RequestBody TimeOffRequest request);

    @DeleteMapping("/time-off/{id}")
    public ResponseEntity<Void> deleteTimeOff(@PathVariable String id);
}
```

#### 4.3 Client Management
Create `ClientController.java`:

```java
@RestController
@RequestMapping("/api/psychologist/clients")
@PreAuthorize("hasRole('PSYCHOLOGIST')")
public class ClientController {

    @GetMapping
    public ResponseEntity<List<ClientSummaryDTO>> getMyClients();
    // Returns all clients who have had appointments with this psychologist

    @GetMapping("/{clientId}/appointments")
    public ResponseEntity<List<AppointmentDTO>> getClientAppointments(@PathVariable String clientId);

    @GetMapping("/{clientId}/messages")
    public ResponseEntity<List<MessageDTO>> getClientMessages(@PathVariable String clientId);
}
```

#### 4.4 Services
Create services in `backend/src/main/java/com/groundandgrow/service/`:

**PsychologistService.java**
```java
@Service
public class PsychologistService {

    public PsychologistDTO getProfile(String psychologistId);

    public PsychologistDTO updateProfile(String psychologistId, UpdateProfileRequest request);

    public DashboardDTO getDashboard(String psychologistId) {
        // Get today's appointments
        // Get upcoming appointments (next 7 days)
        // Calculate stats: total sessions, pending bookings, etc.
    }

    public List<GuestBooking> getAppointments(String psychologistId, LocalDate start, LocalDate end);

    public GuestBooking updateAppointmentStatus(String appointmentId, String status);
}
```

**AvailabilityManagementService.java**
```java
@Service
public class AvailabilityManagementService {

    public Availability addAvailability(String psychologistId, AvailabilityRequest request);

    public Availability updateAvailability(String availabilityId, AvailabilityRequest request);

    public void deleteAvailability(String availabilityId);

    public TimeOff addTimeOff(String psychologistId, TimeOffRequest request);

    public void deleteTimeOff(String timeOffId);
}
```

#### 4.5 DTOs
Create DTOs in `backend/src/main/java/com/groundandgrow/dto/`:

- **DashboardDTO.java** - Dashboard statistics
- **AppointmentDTO.java** - Appointment details
- **ClientSummaryDTO.java** - Client summary
- **UpdateProfileRequest.java**
- **UpdateStatusRequest.java**
- **NotesRequest.java**
- **AvailabilityRequest.java**
- **TimeOffRequest.java**

#### 4.6 Testing
- Test all psychologist endpoints with JWT authentication
- Test authorization (psychologist can only access their own data)
- Test dashboard statistics calculation
- Test availability CRUD operations

---

## Agent 5: Frontend Application

### Responsibility
Build the React frontend with dual portals for patients and psychologists.

### Tasks

#### 5.1 Project Structure Setup
Organize `frontend/src/` with proper structure:

```
src/
├── components/
│   ├── common/          # Shared components
│   ├── patient/         # Patient portal components
│   └── psychologist/    # Psychologist portal components
├── pages/
│   ├── patient/         # Patient portal pages
│   └── psychologist/    # Psychologist portal pages
├── services/            # API services
├── store/               # Redux store
├── hooks/               # Custom hooks
├── utils/               # Utility functions
├── types/               # TypeScript types
└── assets/              # Images, icons
```

#### 5.2 API Service Layer
Create API services in `frontend/src/services/`:

**api.ts** (already created - enhance it)
```typescript
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
});

// Add request/response interceptors (already done)
```

**authService.ts**
```typescript
export const authService = {
  login: async (email: string, password: string) => {
    const response = await api.post('/auth/login', { email, password });
    localStorage.setItem('authToken', response.data.token);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('authToken');
  },

  getCurrentUser: async () => {
    return await api.get('/psychologist/profile');
  }
};
```

**bookingService.ts**
```typescript
export const bookingService = {
  getPsychologists: async () => {
    return await api.get('/public/psychologists');
  },

  getAvailability: async (psychologistId: string, date: string) => {
    return await api.get(`/public/psychologists/${psychologistId}/availability?date=${date}`);
  },

  createBooking: async (bookingData: GuestBookingRequest) => {
    return await api.post('/public/bookings', bookingData);
  },

  getBookingByToken: async (token: string) => {
    return await api.get(`/public/bookings/${token}`);
  },

  cancelBooking: async (token: string) => {
    return await api.put(`/public/bookings/${token}/cancel`);
  }
};
```

**psychologistService.ts**
```typescript
export const psychologistService = {
  getDashboard: async () => {
    return await api.get('/psychologist/dashboard');
  },

  getAppointments: async (params?: AppointmentParams) => {
    return await api.get('/psychologist/appointments', { params });
  },

  updateAppointmentStatus: async (id: string, status: string) => {
    return await api.put(`/psychologist/appointments/${id}/status`, { status });
  },

  getAvailability: async () => {
    return await api.get('/psychologist/availability');
  },

  addAvailability: async (data: AvailabilityRequest) => {
    return await api.post('/psychologist/availability', data);
  }
};
```

#### 5.3 TypeScript Types
Create types in `frontend/src/types/`:

**booking.types.ts**
```typescript
export interface Psychologist {
  id: string;
  firstName: string;
  lastName: string;
  specialization: string;
  bio: string;
}

export interface SessionType {
  id: string;
  name: string;
  description: string;
  durationMinutes: number;
  price: number;
  modality: 'online' | 'in_person' | 'phone';
}

export interface GuestBookingRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  psychologistId: string;
  sessionTypeId: string;
  appointmentDateTime: string;
  modality: string;
  notes?: string;
}

export interface GuestBooking {
  id: string;
  confirmationToken: string;
  // ... other fields
}
```

**auth.types.ts**, **psychologist.types.ts**, etc.

#### 5.4 Redux Store
Set up Redux slices in `frontend/src/store/slices/`:

**authSlice.ts**
```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface AuthState {
  token: string | null;
  user: any | null;
  isAuthenticated: boolean;
}

const initialState: AuthState = {
  token: localStorage.getItem('authToken'),
  user: null,
  isAuthenticated: !!localStorage.getItem('authToken')
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action: PayloadAction<{ token: string; user: any }>) => {
      state.token = action.payload.token;
      state.user = action.payload.user;
      state.isAuthenticated = true;
    },
    logout: (state) => {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      localStorage.removeItem('authToken');
    }
  }
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
```

**bookingSlice.ts** - For patient booking flow state

Update `store.ts` to include all reducers.

#### 5.5 Patient Portal Pages
Create pages in `frontend/src/pages/patient/`:

**HomePage.tsx**
```tsx
// Landing page with:
// - Hero section
// - Browse psychologists
// - How it works section
// - Call to action (Book Now)
```

**PsychologistListPage.tsx**
```tsx
// Display all psychologists with:
// - Filters (specialization, modality)
// - Psychologist cards (photo, name, bio, specialization)
// - "Book Appointment" button for each
```

**BookingPage.tsx**
```tsx
// Multi-step booking form:
// Step 1: Select session type
// Step 2: Select date and time
// Step 3: Enter personal information
// Step 4: Review and proceed to payment
// On submit: Create booking → Redirect to Stripe
```

**BookingSuccessPage.tsx**
```tsx
// After successful payment:
// - Thank you message
// - Appointment details
// - What's next (you'll receive email/SMS)
// - Management link
```

**ManageBookingPage.tsx**
```tsx
// Accessed via confirmation token from email
// Display:
// - Appointment details
// - Cancel booking button
// - Reschedule button
// - Contact information
```

#### 5.6 Psychologist Portal Pages
Create pages in `frontend/src/pages/psychologist/`:

**LoginPage.tsx**
```tsx
// Login form:
// - Email input
// - Password input
// - Submit button
// - Error handling
```

**DashboardPage.tsx**
```tsx
// Psychologist dashboard:
// - Welcome message
// - Today's appointments
// - Upcoming appointments (7 days)
// - Quick stats (total sessions, pending, etc.)
// - Quick actions (view calendar, manage availability)
```

**AppointmentsPage.tsx**
```tsx
// List all appointments:
// - Filters (date range, status)
// - Appointment cards with details
// - Actions: view details, mark completed, add notes
// - Calendar view option
```

**AvailabilityPage.tsx**
```tsx
// Manage availability:
// - Weekly schedule view
// - Add/edit/delete recurring availability
// - Add/delete time off
// - Preview of available slots
```

**MessagesPage.tsx**
```tsx
// Messaging interface:
// - List of conversations
// - Message thread view
// - Send message form
// - Unread count indicator
```

**ProfilePage.tsx**
```tsx
// Psychologist profile:
// - View/edit personal information
// - Bio and specialization
// - Contact information
```

#### 5.7 Reusable Components
Create in `frontend/src/components/common/`:

- **Button.tsx** - Styled button component
- **Input.tsx** - Form input component
- **Card.tsx** - Card wrapper
- **Modal.tsx** - Modal dialog
- **LoadingSpinner.tsx** - Loading indicator
- **DatePicker.tsx** - Date selection
- **TimePicker.tsx** - Time selection
- **Navbar.tsx** - Navigation bar (different for patient/psychologist)
- **Footer.tsx** - Footer component

#### 5.8 Routing
Update `App.tsx`:

```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store/store';

// Patient pages
import HomePage from './pages/patient/HomePage';
import PsychologistListPage from './pages/patient/PsychologistListPage';
import BookingPage from './pages/patient/BookingPage';
import BookingSuccessPage from './pages/patient/BookingSuccessPage';
import ManageBookingPage from './pages/patient/ManageBookingPage';

// Psychologist pages
import LoginPage from './pages/psychologist/LoginPage';
import DashboardPage from './pages/psychologist/DashboardPage';
import AppointmentsPage from './pages/psychologist/AppointmentsPage';
import AvailabilityPage from './pages/psychologist/AvailabilityPage';
import MessagesPage from './pages/psychologist/MessagesPage';

// Components
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          {/* Patient Portal - Public */}
          <Route path="/" element={<HomePage />} />
          <Route path="/psychologists" element={<PsychologistListPage />} />
          <Route path="/book/:psychologistId" element={<BookingPage />} />
          <Route path="/booking/success" element={<BookingSuccessPage />} />
          <Route path="/booking/manage/:token" element={<ManageBookingPage />} />

          {/* Psychologist Portal - Protected */}
          <Route path="/psychologist/login" element={<LoginPage />} />
          <Route path="/psychologist" element={<ProtectedRoute />}>
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="appointments" element={<AppointmentsPage />} />
            <Route path="availability" element={<AvailabilityPage />} />
            <Route path="messages" element={<MessagesPage />} />
            <Route path="profile" element={<ProfilePage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
  );
}
```

#### 5.9 Protected Route Component
Create `frontend/src/components/ProtectedRoute.tsx`:

```tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store/store';

const ProtectedRoute = () => {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  return isAuthenticated ? <Outlet /> : <Navigate to="/psychologist/login" />;
};

export default ProtectedRoute;
```

#### 5.10 Styling
Use Tailwind CSS throughout. Create a theme configuration if needed.

Example component styling:
```tsx
<button className="bg-primary-600 hover:bg-primary-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors">
  Book Appointment
</button>
```

#### 5.11 Testing
- Test all pages render correctly
- Test form submissions
- Test protected routes
- Test API integration
- Test responsive design
- Test booking flow end-to-end

---

## Coordination Between Agents

### Communication Protocol

1. **Shared Context**: All agents must read `.claude/context.md` and `FEATURES.md` before starting
2. **No Overlapping Code**: Each agent has distinct responsibilities
3. **Standard Patterns**: Follow Spring Boot and React best practices
4. **Naming Conventions**: Use consistent naming across all code
5. **Git Workflow** (if using version control):
   - Agent 1: `feature/backend-core`
   - Agent 2: `feature/booking-payment`
   - Agent 3: `feature/notifications`
   - Agent 4: `feature/psychologist-portal`
   - Agent 5: `feature/frontend`

### Dependencies Between Agents

#### Agent 1 → Agent 2
- Agent 2 needs: Models (GuestBooking, Psychologist, SessionType, Availability)
- Agent 2 needs: Repositories
- **Wait for Agent 1 to complete models before starting**

#### Agent 1 → Agent 4
- Agent 4 needs: JWT infrastructure
- Agent 4 needs: Models and repositories
- **Wait for Agent 1 to complete auth system**

#### Agent 2, 3, 4 → Agent 5
- Agent 5 needs backend APIs to be functional
- **Agent 5 can start with UI mockups, but needs backends for integration**

#### Agent 3 → Agent 2
- Agent 2 calls notification services
- Agent 3 can work in parallel, but Agent 2 needs EmailService and SmsService interfaces

### Integration Points

1. **After Agent 1 completes**: Agents 2 and 4 can start
2. **After Agent 2 completes**: Guest booking flow can be tested
3. **After Agent 3 completes**: Full notification flow can be tested
4. **After Agent 4 completes**: Psychologist portal backend ready
5. **Agent 5 integrates**: Once all backend APIs are ready

---

## Testing Strategy

### Unit Testing
Each agent tests their own components:
- Agent 1: JWT, repositories
- Agent 2: Booking service, availability calculation
- Agent 3: Notification scheduling, email/SMS sending
- Agent 4: Psychologist services
- Agent 5: React components, Redux reducers

### Integration Testing
After all agents complete:
- End-to-end booking flow
- Payment webhook handling
- Notification delivery
- Psychologist portal workflows

### Manual Testing Checklist
- [ ] Guest can book appointment without login
- [ ] Stripe payment succeeds
- [ ] Confirmation email received
- [ ] Confirmation SMS received
- [ ] Booking can be cancelled via email link
- [ ] Refund processed correctly
- [ ] Reminder sent 24 hours before appointment
- [ ] Psychologist can login
- [ ] Psychologist sees bookings on dashboard
- [ ] Psychologist can manage availability
- [ ] Psychologist can send messages
- [ ] Messaging notifications sent

---

## Deployment Checklist

Before going live:

1. **Environment Variables**
   - [ ] Set all production credentials
   - [ ] Use live Stripe API keys
   - [ ] Configure production MongoDB (Atlas)
   - [ ] Set up production email service
   - [ ] Set up production SMS service

2. **Security**
   - [ ] Change all default passwords
   - [ ] Use strong JWT secret (256-bit)
   - [ ] Enable HTTPS only
   - [ ] Configure CORS properly
   - [ ] Enable rate limiting

3. **Monitoring**
   - [ ] Set up error logging
   - [ ] Monitor Stripe webhooks
   - [ ] Monitor notification delivery
   - [ ] Set up uptime monitoring

4. **Compliance**
   - [ ] Privacy policy published
   - [ ] Terms of service published
   - [ ] Cookie consent implemented
   - [ ] Data retention policy defined

---

## Estimated Timeline

- **Agent 1**: 2-3 days (backend core + auth)
- **Agent 2**: 2-3 days (booking + payment)
- **Agent 3**: 1-2 days (notifications)
- **Agent 4**: 1-2 days (psychologist APIs)
- **Agent 5**: 3-4 days (frontend)

**Total (Parallel)**: 3-4 days with 5 agents working simultaneously

**Total (Sequential)**: 9-14 days with 1 agent

---

## Success Criteria

The application is complete when:

1. ✅ A guest can book an appointment and pay via Stripe (no login)
2. ✅ Email and SMS confirmations are sent automatically
3. ✅ Reminders are sent 24 hours before appointments
4. ✅ Guests can cancel bookings via email link
5. ✅ Refunds are processed automatically
6. ✅ Psychologists can login securely
7. ✅ Psychologists can view all bookings
8. ✅ Psychologists can manage their availability
9. ✅ Secure messaging works between psychologist and clients
10. ✅ All tests pass
11. ✅ Application is deployed and accessible

---

## Support Resources

- **Stripe Documentation**: https://stripe.com/docs
- **Twilio Documentation**: https://www.twilio.com/docs
- **SendGrid Documentation**: https://docs.sendgrid.com
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **React Documentation**: https://react.dev
- **MongoDB Documentation**: https://www.mongodb.com/docs

---

## Contact & Questions

If you encounter any issues or need clarification:
1. Review `.claude/context.md` and `FEATURES.md`
2. Check this document for guidance
3. Review Spring Boot / React documentation
4. Consult with other agents if there are integration questions

Good luck building Ground & Grow Psychology! 🚀
