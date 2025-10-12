# In-House Development Roadmap
## Ground & Grow Psychology Platform

**Version:** 1.0
**Last Updated:** 2025-10-05
**Focus:** Development tasks that do not require 3rd party vendor subscriptions

---

## Overview

This roadmap prioritizes development tasks that can be completed entirely in-house without requiring paid subscriptions to external services (Stripe, Twilio, SendGrid). These external integrations are already built and can be configured later when ready for production deployment.

---

## Priority Levels

- **P0 (Critical):** Must be completed before any testing
- **P1 (High):** Important for core functionality
- **P2 (Medium):** Enhances user experience
- **P3 (Low):** Nice to have, future improvements

---

## Phase 1: Local Development Environment Setup
**Timeline:** 1-2 days
**Status:** Ready to Start

### P0 Tasks

- [ ] **Set up local MongoDB instance**
  - Install MongoDB Community Edition
  - Create `groundandgrow` database
  - Verify connection on `localhost:27017`
  - *Alternative:* Use MongoDB Atlas free tier (M0)

- [ ] **Configure backend environment variables (local)**
  ```bash
  MONGODB_URI=mongodb://localhost:27017/groundandgrow
  JWT_SECRET=local-dev-secret-change-in-production-min-256-bits
  ADMIN_USERNAME=admin
  ADMIN_PASSWORD=admin123
  FRONTEND_URL=http://localhost:5173

  # Disable 3rd party services for now
  EMAIL_ENABLED=false
  SMS_ENABLED=false
  TWILIO_ENABLED=false
  SENDGRID_ENABLED=false

  # Use mock/console logging instead
  MAIL_HOST=localhost
  MAIL_PORT=1025  # MailHog/MailDev for local email testing
  ```

- [ ] **Configure frontend environment variables**
  ```bash
  VITE_API_BASE_URL=http://localhost:8080/api
  VITE_APP_NAME=Ground & Grow Psychology
  ```

- [ ] **Verify backend starts successfully**
  ```bash
  cd backend
  mvn clean install
  mvn spring-boot:run
  ```

- [ ] **Verify frontend starts successfully**
  ```bash
  cd frontend
  npm install
  npm run dev
  ```

- [ ] **Create initial admin user in MongoDB**
  - Use DataInitializer or manual MongoDB insert
  - Hash password with BCrypt
  - Test login at `/psychologist/login`

---

## Phase 2: Mock External Services
**Timeline:** 2-3 days
**Status:** Ready to Start

### P0 Tasks - Email Mocking

- [ ] **Set up MailHog or MailDev (local email testing)**
  - Install MailHog: `docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`
  - Or install MailDev: `npm install -g maildev && maildev`
  - Access web UI at `http://localhost:8025`
  - Configure backend to use SMTP: `localhost:1025`

- [ ] **Create MockEmailService (alternative approach)**
  ```java
  @Service
  @Profile("dev")
  public class MockEmailService extends EmailService {
      @Override
      public void sendTemplated(String to, String subject, String template, Map<String, Object> variables) {
          log.info("📧 MOCK EMAIL");
          log.info("To: {}", to);
          log.info("Subject: {}", subject);
          log.info("Template: {}", template);
          log.info("Variables: {}", variables);
          // Optionally write to file for inspection
      }
  }
  ```

### P0 Tasks - SMS Mocking

- [ ] **Create MockSmsService**
  ```java
  @Service
  @Profile("dev")
  public class MockSmsService extends SmsService {
      @Override
      public void sendSms(String to, String message) {
          log.info("📱 MOCK SMS");
          log.info("To: {}", to);
          log.info("Message: {}", message);
          // Optionally write to file or in-memory log
      }
  }
  ```

### P0 Tasks - Payment Mocking

- [ ] **Create MockStripeService**
  ```java
  @Service
  @Profile("dev")
  public class MockStripeService extends StripeService {
      @Override
      public CheckoutSessionResponse createCheckoutSession(GuestBooking booking) {
          // Generate mock checkout URL
          String mockUrl = "http://localhost:5173/mock-payment/" + booking.getId();
          String mockSessionId = "mock_session_" + UUID.randomUUID();
          return new CheckoutSessionResponse(mockUrl, mockSessionId);
      }

      @Override
      public void processRefund(String paymentIntentId, Long amount) {
          log.info("💳 MOCK REFUND: {} for amount {}", paymentIntentId, amount);
      }
  }
  ```

- [ ] **Create mock payment page in frontend**
  - Route: `/mock-payment/:bookingId`
  - Buttons: "Simulate Success" | "Simulate Failure"
  - On success: Call mock webhook endpoint
  - On failure: Show error message

- [ ] **Create mock webhook endpoint (dev only)**
  ```java
  @RestController
  @RequestMapping("/api/dev/mock-webhooks")
  @Profile("dev")
  public class MockWebhookController {
      @PostMapping("/stripe/success/{sessionId}")
      public ResponseEntity<?> mockPaymentSuccess(@PathVariable String sessionId) {
          guestBookingService.handlePaymentSuccess(sessionId);
          return ResponseEntity.ok().build();
      }

      @PostMapping("/stripe/failure/{sessionId}")
      public ResponseEntity<?> mockPaymentFailure(@PathVariable String sessionId) {
          guestBookingService.handlePaymentFailure(sessionId);
          return ResponseEntity.ok().build();
      }
  }
  ```

---

## Phase 3: Core Functionality Testing
**Timeline:** 3-5 days
**Status:** Ready to Start

### P1 Tasks - Authentication

- [ ] **Test psychologist registration (admin only)**
  - Use Postman/Insomnia or Swagger UI
  - Create test psychologist accounts
  - Verify password hashing (BCrypt)

- [ ] **Test psychologist login**
  - Login via frontend: `/psychologist/login`
  - Verify JWT token generation
  - Verify token stored in localStorage and Redux
  - Check token expiration (24 hours)

- [ ] **Test token refresh**
  - Call `/api/auth/refresh` endpoint
  - Verify new token returned

- [ ] **Test protected routes**
  - Access `/psychologist/dashboard` without token → should redirect to login
  - Access with valid token → should load dashboard
  - Access with expired token → should redirect to login

### P1 Tasks - Session Types & Availability

- [ ] **Create session types via MongoDB**
  - Initial Consultation (60 min, $150, ONLINE)
  - Follow-up Session (50 min, $120, ONLINE/IN_PERSON)
  - Crisis Session (90 min, $200, ONLINE/PHONE)

- [ ] **Set up psychologist availability**
  - Use frontend: `/psychologist/availability`
  - Add recurring availability (e.g., Mon-Fri 9am-5pm)
  - Test add/edit/delete availability slots
  - Add time off period (e.g., vacation)

- [ ] **Test availability calculation**
  - Navigate to booking page as patient
  - Select psychologist and date
  - Verify available slots show correctly
  - Create a booking (mock payment)
  - Verify that slot is no longer available

### P1 Tasks - Guest Booking Flow (with Mock Payment)

- [ ] **Test complete booking flow**
  - Step 1: Browse psychologists (`/psychologists`)
  - Step 2: Select psychologist, click "Book Appointment"
  - Step 3: Complete 4-step booking form
  - Step 4: Submit → redirected to mock payment page
  - Step 5: Click "Simulate Success"
  - Step 6: Verify booking status updated to CONFIRMED
  - Step 7: Check mock email in MailHog
  - Step 8: Check mock SMS in console logs

- [ ] **Test booking management via token**
  - Navigate to `/booking/manage/{token}`
  - Verify booking details displayed
  - Test "Cancel Booking" button
  - Verify cancellation confirmation (mock email/SMS)
  - Test "Reschedule" functionality

### P1 Tasks - Psychologist Dashboard

- [ ] **Test dashboard statistics**
  - Create several test bookings
  - Mark some as completed, cancelled, no-show
  - Verify statistics calculated correctly:
    - Total sessions
    - Pending bookings
    - Completed this week/month
    - Cancelled/no-shows this month

- [ ] **Test appointment management**
  - Filter appointments by date range
  - Filter by status (confirmed, completed, cancelled)
  - Mark appointment as completed
  - Mark appointment as no-show
  - Add psychologist notes

- [ ] **Test client management**
  - View all clients (grouped by email)
  - View client appointment history
  - Verify statistics (total appointments, completed, cancelled)

---

## Phase 4: Messaging System
**Timeline:** 2-3 days
**Status:** Ready to Start

### P1 Tasks - Backend Messaging

- [ ] **Test message creation**
  - Send message from psychologist to client (use email as clientId for guest bookings)
  - Verify message saved to MongoDB
  - Verify threadId generated correctly
  - Check mock email notification sent

- [ ] **Test message retrieval**
  - Get all messages for user
  - Get messages by thread
  - Get unread messages
  - Count unread messages

- [ ] **Test read receipts**
  - Mark message as read
  - Verify `isRead` flag updated
  - Verify `readAt` timestamp set

### P2 Tasks - Frontend Messaging UI

- [ ] **Implement MessagesPage component**
  - Currently a placeholder
  - Create conversation list UI
  - Create message thread view
  - Create send message form
  - Display unread count badge

- [ ] **Integrate with backend APIs**
  - Fetch messages on component mount
  - Send message on form submit
  - Mark as read when message viewed
  - Auto-refresh messages (polling or WebSocket in future)

---

## Phase 5: Data Validation & Error Handling
**Timeline:** 2-3 days
**Status:** Ready to Start

### P1 Tasks - Backend Validation

- [ ] **Test input validation**
  - Submit booking with missing required fields
  - Submit booking with invalid email format
  - Submit booking with invalid phone format
  - Verify 400 Bad Request returned with error messages

- [ ] **Test business logic validation**
  - Try to book a slot that's already taken
  - Try to book in the past
  - Try to book outside availability hours
  - Try to book during time off period
  - Verify appropriate error messages

- [ ] **Test authorization**
  - Psychologist tries to access another psychologist's data
  - Verify 403 Forbidden returned
  - Guest tries to access protected endpoints without token
  - Verify 401 Unauthorized returned

### P2 Tasks - Frontend Validation

- [ ] **Add form validation to booking form**
  - Required field validation
  - Email format validation
  - Phone format validation
  - Date/time validation
  - Real-time error messages

- [ ] **Add form validation to psychologist forms**
  - Login form validation
  - Profile update validation
  - Availability form validation
  - Time off form validation

- [ ] **Improve error handling**
  - Display API errors to user (toast notifications or alerts)
  - Handle network errors gracefully
  - Show loading states during API calls
  - Add retry mechanisms for failed requests

---

## Phase 6: Testing Infrastructure
**Timeline:** 3-5 days
**Status:** Ready to Start

### P1 Tasks - Backend Testing

- [ ] **Write unit tests for services**
  - GuestBookingService tests (mock dependencies)
  - AvailabilityService tests (slot calculation logic)
  - NotificationSchedulerService tests
  - MessageService tests
  - PsychologistService tests

- [ ] **Write integration tests for controllers**
  - AuthController (login, register)
  - GuestBookingController (create, cancel, reschedule)
  - PsychologistController (dashboard, appointments)
  - AvailabilityController (CRUD operations)
  - MessageController (send, retrieve, mark read)

- [ ] **Set up test database**
  - Use embedded MongoDB for tests
  - Create test data fixtures
  - Ensure tests are isolated (clean database between tests)

- [ ] **Achieve 80%+ code coverage**
  - Run: `mvn test jacoco:report`
  - Review coverage report in `target/site/jacoco/index.html`
  - Add tests for uncovered code

### P1 Tasks - Frontend Testing

- [ ] **Write component tests**
  - LoginPage component
  - BookingPage component (each step)
  - DashboardPage component
  - Common components (Button, Input, Card, Modal)

- [ ] **Write integration tests**
  - Test full booking flow (mock API)
  - Test login and navigation
  - Test protected route guards
  - Test form submissions

- [ ] **Set up test utilities**
  - Create API mocking helpers (MSW - Mock Service Worker)
  - Create test data factories
  - Create custom render function with Redux/Router providers

- [ ] **Run tests in CI**
  - Configure test scripts in package.json
  - Ensure tests pass before merge

---

## Phase 7: UI/UX Improvements
**Timeline:** 3-5 days
**Status:** Ready to Start

### P2 Tasks - Patient Portal

- [ ] **Improve HomePage**
  - Add hero section with compelling copy
  - Add "How It Works" section
  - Add testimonials section (placeholder content)
  - Add FAQ section
  - Improve mobile responsiveness

- [ ] **Improve PsychologistListPage**
  - Add search functionality (by name, specialization)
  - Add filter by modality (online, in-person, phone)
  - Add filter by availability (next available)
  - Add psychologist profile images (placeholder avatars)
  - Add rating/reviews display (future feature, use placeholders)

- [ ] **Improve BookingPage**
  - Add progress indicator for 4 steps
  - Add "Back" button navigation
  - Add booking summary sidebar (shows selections as user progresses)
  - Add better date/time picker UI
  - Add session type comparison view

- [ ] **Improve BookingSuccessPage**
  - Add animation/confetti on success
  - Add calendar download button (.ics file)
  - Add social sharing (optional)

- [ ] **Improve ManageBookingPage**
  - Add countdown to appointment
  - Add preparation checklist
  - Add directions/meeting link display
  - Improve cancellation flow with confirmation dialog

### P2 Tasks - Psychologist Portal

- [ ] **Improve DashboardPage**
  - Add charts for statistics (using Chart.js or Recharts)
  - Add calendar view of appointments
  - Add quick actions (shortcuts to common tasks)
  - Add notifications/alerts section

- [ ] **Improve AppointmentsPage**
  - Add calendar view (in addition to list view)
  - Add export to CSV functionality
  - Add bulk actions (mark multiple as completed)
  - Add appointment details modal (instead of navigation)

- [ ] **Improve AvailabilityPage**
  - Add visual weekly schedule grid
  - Add drag-and-drop for availability blocks
  - Add templates for common schedules
  - Add copy schedule to future weeks

- [ ] **Improve ProfilePage**
  - Add profile image upload (local storage for now)
  - Add richer bio editor (markdown support)
  - Add certifications/qualifications section
  - Add languages spoken

### P2 Tasks - Common UI

- [ ] **Create design system**
  - Define color palette (already using Tailwind)
  - Define typography scale
  - Create component library documentation
  - Ensure consistent spacing and sizing

- [ ] **Improve accessibility (a11y)**
  - Add ARIA labels to all interactive elements
  - Ensure keyboard navigation works
  - Add focus indicators
  - Test with screen reader
  - Add skip-to-content links

- [ ] **Add loading states**
  - Skeleton screens for data loading
  - Loading spinners for actions
  - Progress bars for multi-step processes

- [ ] **Add toast notifications**
  - Success messages (booking created, profile updated)
  - Error messages (API failures)
  - Info messages (reminders, tips)
  - Use library like react-toastify or build custom

---

## Phase 8: Data Management & Admin Tools
**Timeline:** 2-3 days
**Status:** Ready to Start

### P2 Tasks - Admin Dashboard (Future)

- [ ] **Create admin portal (optional)**
  - Route: `/admin` (protected, ADMIN role only)
  - View all psychologists
  - View all bookings
  - View all clients
  - View system statistics
  - Manage session types

- [ ] **Database seeding script**
  - Create script to populate test data
  - 5-10 sample psychologists
  - 20-30 sample bookings (various statuses)
  - Sample session types
  - Sample messages

- [ ] **Data export functionality**
  - Export bookings to CSV
  - Export client list to CSV
  - Export messages to CSV
  - Use in psychologist portal

---

## Phase 9: Performance Optimization
**Timeline:** 2-3 days
**Status:** Ready to Start

### P2 Tasks - Backend Optimization

- [ ] **Add database indexes**
  - Review query patterns
  - Add indexes for frequently queried fields
  - Add compound indexes for multi-field queries
  - Verify indexes created: `db.collection.getIndexes()`

- [ ] **Optimize N+1 queries**
  - Review service methods
  - Use MongoDB aggregation pipeline where appropriate
  - Fetch related data in single query instead of loops

- [ ] **Add response caching (Spring Cache)**
  - Cache psychologist list (1 hour TTL)
  - Cache session types (1 hour TTL)
  - Cache availability (5 minutes TTL)
  - Use in-memory cache (Caffeine) for development

- [ ] **Add pagination**
  - Appointments list (page size: 20)
  - Client list (page size: 50)
  - Messages list (page size: 50)
  - Use Spring Data Pageable

### P2 Tasks - Frontend Optimization

- [ ] **Implement code splitting**
  - Use React.lazy() for route-based splitting
  - Lazy load heavy components (charts, editors)
  - Measure bundle size: `npm run build -- --report`

- [ ] **Optimize images**
  - Use WebP format for images
  - Add lazy loading for images
  - Use appropriate image sizes (responsive)

- [ ] **Add request debouncing**
  - Debounce search inputs (300ms)
  - Debounce autocomplete (500ms)
  - Throttle scroll events

- [ ] **Implement virtual scrolling**
  - For long appointment lists
  - For long message threads
  - Use react-window or react-virtualized

---

## Phase 10: Developer Experience
**Timeline:** 2-3 days
**Status:** Ready to Start

### P3 Tasks - Development Tools

- [ ] **Set up hot reload for backend**
  - Spring Boot DevTools already included
  - Verify automatic restart on code changes
  - Configure IDE for optimal experience

- [ ] **Improve API documentation**
  - Add more detailed Swagger annotations
  - Add request/response examples
  - Add error response documentation
  - Group endpoints by functionality

- [ ] **Create Postman/Insomnia collection**
  - Export Swagger as Postman collection
  - Add environment variables
  - Add common test scenarios
  - Share with team

- [ ] **Set up code formatting**
  - Backend: Configure Checkstyle or Spotless
  - Frontend: Configure Prettier
  - Add pre-commit hooks (Husky)
  - Ensure consistent code style

- [ ] **Add code linting**
  - Backend: Configure SonarLint
  - Frontend: ESLint already configured
  - Fix linting errors
  - Add to CI pipeline

### P3 Tasks - Documentation

- [ ] **Create API usage examples**
  - Add code snippets for common operations
  - Create cookbook for frequent tasks
  - Document authentication flow with examples

- [ ] **Create developer onboarding guide**
  - Prerequisites and installation
  - Local setup steps
  - How to run tests
  - How to debug
  - Common issues and solutions

- [ ] **Update README.md**
  - Add screenshots
  - Add GIF demos of key features
  - Update setup instructions based on learnings
  - Add troubleshooting section

---

## Phase 11: Code Quality & Refactoring
**Timeline:** 3-5 days
**Status:** Ready to Start

### P2 Tasks - Backend Refactoring

- [ ] **Extract common utilities**
  - Date/time formatting utilities
  - Phone number formatting utilities
  - Email validation utilities
  - Create util package

- [ ] **Improve error handling**
  - Create custom exception classes
  - Create global exception handler (@ControllerAdvice)
  - Return consistent error response format
  - Add error codes for client handling

- [ ] **Add request/response logging**
  - Log all API requests (method, path, status, duration)
  - Log request/response bodies (exclude sensitive data)
  - Use AOP for cross-cutting logging
  - Configure log levels per environment

- [ ] **Refactor long methods**
  - Identify methods > 50 lines
  - Extract helper methods
  - Improve readability
  - Add JavaDoc comments

### P2 Tasks - Frontend Refactoring

- [ ] **Extract common hooks**
  - useApi hook (API call with loading/error states)
  - useAuth hook (authentication utilities)
  - useForm hook (form state management)
  - useDebounce hook (input debouncing)

- [ ] **Extract common utilities**
  - Date formatting utilities
  - Currency formatting utilities
  - Validation utilities
  - Create utils folder

- [ ] **Improve component organization**
  - Move large components to separate files
  - Extract repeated JSX into subcomponents
  - Use composition over prop drilling
  - Add PropTypes or improve TypeScript types

- [ ] **Standardize error handling**
  - Create ErrorBoundary component
  - Create useErrorHandler hook
  - Display user-friendly error messages
  - Log errors to console (or future error tracking service)

---

## Phase 12: Local Deployment & Testing
**Timeline:** 2-3 days
**Status:** Ready to Start

### P1 Tasks - Docker Setup

- [ ] **Complete Docker Compose configuration**
  - Review `docker/docker-compose.yml`
  - Add services: MongoDB, Backend, Frontend
  - Add environment variables
  - Add volume mounts for persistence

- [ ] **Create Dockerfiles**
  - Backend Dockerfile (multi-stage build)
  - Frontend Dockerfile (multi-stage build)
  - Optimize image sizes
  - Use .dockerignore files

- [ ] **Test Docker deployment**
  - Run: `docker-compose up`
  - Verify all services start
  - Verify connectivity between services
  - Test application functionality

- [ ] **Add Docker documentation**
  - Document Docker commands
  - Add troubleshooting steps
  - Document volume management
  - Document networking

### P1 Tasks - End-to-End Testing

- [ ] **Test complete user journeys**
  - Journey 1: Patient books appointment (guest, mock payment)
  - Journey 2: Psychologist manages schedule
  - Journey 3: Psychologist views appointments and clients
  - Journey 4: Patient cancels booking
  - Journey 5: Messaging between psychologist and client

- [ ] **Test error scenarios**
  - Network failure during booking
  - Invalid JWT token
  - Concurrent booking attempts
  - Database connection failure
  - Form validation errors

- [ ] **Test cross-browser compatibility**
  - Chrome
  - Firefox
  - Safari
  - Edge
  - Mobile browsers (iOS Safari, Chrome Android)

- [ ] **Test responsive design**
  - Mobile (375px - 768px)
  - Tablet (768px - 1024px)
  - Desktop (1024px+)
  - Verify all features work on mobile

---

## Deferred Tasks (Require 3rd Party Vendors)

These tasks are **already implemented** in the codebase but require vendor subscriptions to activate:

### Stripe Payment Integration
- Production Stripe account setup
- Live API keys configuration
- Webhook endpoint configuration in Stripe dashboard
- Payment flow testing with real cards
- Refund testing

### Twilio SMS Integration
- Twilio account setup
- Phone number provisioning
- Production SMS testing
- Australian phone number verification
- Cost optimization

### SendGrid Email Integration
- SendGrid account setup
- Domain verification
- Production email sending
- Template management in SendGrid
- Deliverability monitoring

### Cloud Deployment
- AWS account setup (or Azure/GCP)
- EC2 instances or App Service
- MongoDB Atlas production cluster
- S3/CloudFront for frontend
- Load balancer configuration
- Domain and SSL certificates
- CI/CD pipeline to production

---

## Success Metrics (In-House Development)

### Development Milestones

- [ ] All services start successfully in local environment
- [ ] Complete booking flow works with mock payment
- [ ] All backend tests pass (80%+ coverage)
- [ ] All frontend tests pass
- [ ] Application runs in Docker
- [ ] All core features tested manually
- [ ] No critical bugs in issue tracker
- [ ] Documentation complete and accurate

### Code Quality Metrics

- [ ] Backend test coverage: > 80%
- [ ] Frontend test coverage: > 70%
- [ ] Zero critical security vulnerabilities (SonarQube/Snyk)
- [ ] Zero high-priority linting errors
- [ ] All API endpoints documented in Swagger
- [ ] All components have TypeScript types

### Performance Metrics (Local)

- [ ] API response time: < 1s (p95)
- [ ] Frontend page load: < 3s
- [ ] Build time (backend): < 2 minutes
- [ ] Build time (frontend): < 1 minute
- [ ] Docker startup time: < 3 minutes

---

## Getting Started (Quick Start)

### Day 1: Environment Setup
1. Install MongoDB locally or create free Atlas cluster
2. Clone repository
3. Configure `.env` files (backend and frontend)
4. Start backend: `cd backend && mvn spring-boot:run`
5. Start frontend: `cd frontend && npm run dev`
6. Access application: `http://localhost:5173`

### Day 2: Mock Services
1. Install MailHog: `docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`
2. Create mock Stripe service (use profile: `dev`)
3. Create mock payment page in frontend
4. Test complete booking flow

### Day 3-5: Core Testing
1. Create test psychologist accounts
2. Set up availability schedules
3. Create test bookings
4. Test appointment management
5. Test messaging system

### Week 2: Testing & Quality
1. Write unit tests
2. Write integration tests
3. Improve UI/UX
4. Add error handling
5. Performance optimization

### Week 3: Polish & Documentation
1. Code refactoring
2. Documentation updates
3. Developer tools
4. Docker deployment
5. End-to-end testing

---

## Estimated Timeline Summary

| Phase | Duration | Priority | Dependencies |
|-------|----------|----------|--------------|
| 1. Local Environment | 1-2 days | P0 | None |
| 2. Mock Services | 2-3 days | P0 | Phase 1 |
| 3. Core Testing | 3-5 days | P1 | Phase 1, 2 |
| 4. Messaging System | 2-3 days | P1 | Phase 1, 2 |
| 5. Validation & Errors | 2-3 days | P1 | Phase 3 |
| 6. Testing Infrastructure | 3-5 days | P1 | Phase 3 |
| 7. UI/UX Improvements | 3-5 days | P2 | Phase 3 |
| 8. Data Management | 2-3 days | P2 | Phase 3 |
| 9. Performance | 2-3 days | P2 | Phase 6 |
| 10. Developer Experience | 2-3 days | P3 | Any time |
| 11. Code Quality | 3-5 days | P2 | Phase 6 |
| 12. Docker & E2E | 2-3 days | P1 | Phase 1-11 |

**Total Estimated Time:** 4-6 weeks (full-time development)

---

## Notes

- All tasks can be completed without any paid 3rd party subscriptions
- External services (Stripe, Twilio, SendGrid) are already integrated and can be enabled later
- Free tiers available: MongoDB Atlas (M0), MailHog (local), Docker (local)
- Focus on core functionality, testing, and code quality
- External vendor configuration is straightforward when ready (just API keys in .env)

---

**Last Updated:** 2025-10-05
**Next Review:** After Phase 3 completion
