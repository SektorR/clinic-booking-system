# Ground & Grow Psychology - Feature Documentation

## Payment System (Stripe Integration)

### Overview
The system uses Stripe for secure payment processing. All patient bookings require immediate payment.

### Implementation
- **Service**: `StripeService.java`
- **Configuration**: `application.yml` (stripe section)
- **Currency**: Australian Dollar (AUD)

### Features
1. **Checkout Session** - Hosted payment page for guest bookings
2. **Payment Intents** - Direct payment processing
3. **Automatic Refunds** - For cancellations within policy
4. **Webhook Support** - Real-time payment status updates

### Stripe Setup Steps
1. Create Stripe account at https://stripe.com
2. Get API keys (test and live)
3. Configure webhook endpoint: `POST /api/webhooks/stripe`
4. Set environment variables:
   - `STRIPE_API_KEY` - Your secret key
   - `STRIPE_WEBHOOK_SECRET` - Webhook signing secret

### Payment Flow
```
Patient → Selects Appointment → Enters Details → Stripe Checkout
→ Payment Success → Booking Confirmed → Email + SMS Sent
```

---

## Notification System

### Email Notifications

#### Providers
- **JavaMail** (Default) - Uses SMTP (Gmail, SendGrid SMTP, etc.)
- **SendGrid API** (Alternative) - Direct API integration

#### Implementation
- **Service**: `EmailService.java`
- **Templates**: Thymeleaf templates in `resources/templates/email/`
- **Configuration**: `application.yml` (spring.mail and sendgrid sections)

#### Email Types
1. **Booking Confirmation**
   - Template: `booking-confirmation.html`
   - Variables: patientName, psychologistName, appointmentDate, appointmentTime, modality

2. **Appointment Reminder**
   - Template: `appointment-reminder.html`
   - Sent: 24 hours before appointment

3. **Cancellation Confirmation**
   - Template: `cancellation-confirmation.html`
   - Sent: Immediately upon cancellation

4. **Payment Receipt**
   - Template: `payment-receipt.html`
   - Sent: After successful payment

#### Setup (JavaMail with Gmail)
1. Enable 2-factor authentication on Gmail
2. Generate App Password
3. Set environment variables:
   - `MAIL_HOST=smtp.gmail.com`
   - `MAIL_PORT=587`
   - `MAIL_USERNAME=your-email@gmail.com`
   - `MAIL_PASSWORD=your-app-password`

#### Setup (SendGrid)
1. Create SendGrid account
2. Get API key
3. Set environment variables:
   - `SENDGRID_ENABLED=true`
   - `SENDGRID_API_KEY=your-api-key`

### SMS Notifications

#### Provider
- **Twilio** - SMS and voice API

#### Implementation
- **Service**: `SmsService.java`
- **Configuration**: `application.yml` (twilio section)
- **Phone Format**: Australian numbers (+61)

#### SMS Types
1. **Booking Confirmation**
2. **Appointment Reminder** (24 hours before)
3. **Cancellation Notification**
4. **Verification Codes** (for booking management)

#### Setup
1. Create Twilio account at https://twilio.com
2. Get phone number
3. Get account SID and auth token
4. Set environment variables:
   - `TWILIO_ENABLED=true`
   - `TWILIO_ACCOUNT_SID=your-account-sid`
   - `TWILIO_AUTH_TOKEN=your-auth-token`
   - `TWILIO_PHONE_NUMBER=+61412345678`

#### Australian Phone Number Formatting
The system automatically formats Australian phone numbers:
- Input: `0412 345 678` or `04 1234 5678`
- Output: `+61412345678` (E.164 format)

---

## Communication System

### Secure Messaging

#### Features
- Thread-based conversations
- Messages linked to appointments
- Read receipts
- Soft delete support
- Attachment support (future)

#### Implementation
- **Model**: `Message.java`
- **Collection**: `messages`

#### Message Flow
```
Psychologist/Patient → Creates Message → System Stores →
Recipient Notified (Email) → Recipient Reads → Read Receipt Sent
```

### Notification Model

#### Purpose
Tracks all outgoing notifications (email and SMS) with status and scheduling.

#### Implementation
- **Model**: `Notification.java`
- **Collection**: `notifications`

#### Notification Types
- `BOOKING_CONFIRMATION`
- `REMINDER`
- `CANCELLATION`
- `RESCHEDULED`
- `MESSAGE_RECEIVED`
- `PAYMENT_CONFIRMATION`

#### Scheduling
- Notifications can be scheduled for future delivery
- Automatic retry on failure
- Status tracking: `PENDING`, `SENT`, `FAILED`, `CANCELLED`

---

## Guest Booking System

### Overview
Patients can book appointments **without creating an account**. This provides the lowest friction booking experience.

### Implementation
- **Model**: `GuestBooking.java`
- **Collection**: `guest_bookings`

### Key Fields
- **confirmationToken** - Unique token for managing booking
- **stripeCheckoutSessionId** - Link to Stripe payment
- **paymentStatus** - Track payment state
- **bookingStatus** - Track appointment state
- **emailConfirmed** - Verify email ownership
- **reminderSent** - Track reminder status

### Booking Flow

#### 1. Browse & Select
- Patient views available psychologists
- Selects appointment time
- Chooses modality (online/in-person/phone)

#### 2. Guest Information
Patient provides:
- First name, last name
- Email address
- Phone number
- Date of birth (optional)
- Any notes/concerns

#### 3. Payment
- System creates Stripe Checkout Session
- Patient redirected to Stripe payment page
- Patient completes payment

#### 4. Confirmation
Upon successful payment:
- Generate unique confirmation token (UUID)
- Create guest booking record
- Send confirmation email with:
  - Appointment details
  - Payment receipt
  - Management link (with token)
- Send confirmation SMS

#### 5. Management
Patient can manage booking via email link:
- `https://groundandgrow.com/booking/manage/{confirmationToken}`
- View appointment details
- Cancel appointment (with refund if within policy)
- Reschedule appointment
- No login required

#### 6. Reminders
24 hours before appointment:
- Automated reminder email sent
- Automated reminder SMS sent
- Flag `reminderSent = true`

### Security Considerations
- Confirmation tokens are UUIDs (cryptographically random)
- Rate limiting on management endpoints
- Email verification recommended
- Phone verification via SMS code (optional)

---

## Database Models

### GuestBooking
```json
{
  "id": "string",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "psychologistId": "string",
  "sessionTypeId": "string",
  "appointmentDateTime": "datetime",
  "durationMinutes": "number",
  "modality": "online|in_person|phone",
  "stripePaymentIntentId": "string",
  "stripeCheckoutSessionId": "string",
  "amount": "number",
  "paymentStatus": "pending|completed|failed|refunded",
  "bookingStatus": "pending_payment|confirmed|cancelled|completed|no_show",
  "confirmationToken": "string (unique)",
  "emailConfirmed": "boolean",
  "reminderSent": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Message
```json
{
  "id": "string",
  "senderId": "string",
  "receiverId": "string",
  "senderType": "CLIENT|PSYCHOLOGIST|SYSTEM",
  "receiverType": "CLIENT|PSYCHOLOGIST",
  "subject": "string",
  "content": "string",
  "appointmentId": "string (optional)",
  "threadId": "string",
  "isRead": "boolean",
  "readAt": "datetime",
  "createdAt": "datetime"
}
```

### Notification
```json
{
  "id": "string",
  "recipientId": "string",
  "recipientType": "CLIENT|PSYCHOLOGIST|GUEST",
  "guestBookingId": "string (optional)",
  "notificationType": "BOOKING_CONFIRMATION|REMINDER|CANCELLATION|...",
  "deliveryMethod": "EMAIL|SMS|BOTH",
  "subject": "string",
  "message": "string",
  "scheduledFor": "datetime",
  "sentAt": "datetime",
  "status": "PENDING|SENT|FAILED|CANCELLED",
  "createdAt": "datetime"
}
```

---

## Environment Configuration

### Required for Production
```bash
# Stripe (Required)
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Email (Choose one)
# Option 1: JavaMail
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=noreply@groundandgrow.com.au
MAIL_PASSWORD=app-password

# Option 2: SendGrid
SENDGRID_ENABLED=true
SENDGRID_API_KEY=SG...

# SMS (Optional but recommended)
TWILIO_ENABLED=true
TWILIO_ACCOUNT_SID=AC...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=+61...

# Frontend URL
FRONTEND_URL=https://groundandgrow.com.au
```

---

## API Endpoints (Planned)

### Guest Booking
- `GET /api/public/psychologists` - List available psychologists
- `GET /api/public/availability/{psychologistId}` - Get available time slots
- `POST /api/public/bookings` - Create guest booking (returns checkout URL)
- `GET /api/public/bookings/{token}` - View booking details
- `PUT /api/public/bookings/{token}/cancel` - Cancel booking
- `PUT /api/public/bookings/{token}/reschedule` - Reschedule booking

### Webhooks
- `POST /api/webhooks/stripe` - Stripe payment webhooks

### Notifications (Internal)
- `POST /api/notifications/send` - Send notification
- `GET /api/notifications/{id}` - Get notification status

### Messages (Authenticated)
- `GET /api/messages` - List messages
- `POST /api/messages` - Send message
- `PUT /api/messages/{id}/read` - Mark as read

---

## Testing

### Stripe Test Mode
Use test cards:
- Success: `4242 4242 4242 4242`
- Decline: `4000 0000 0000 0002`

### Twilio Test Mode
Use verified phone numbers in trial account

### Email Testing
Use a service like Mailtrap.io or Mailhog for development

---

## Compliance & Security

### Australian Privacy Act
- Secure storage of personal information
- Clear privacy policy
- Data retention policies
- Right to access and delete data

### Payment Security
- PCI DSS compliance via Stripe
- No card data stored on servers
- SSL/TLS encryption required

### HIPAA Considerations
- Encrypt sensitive health information
- Audit logs for data access
- Secure messaging
- Business Associate Agreement with service providers

---

## Future Enhancements
- [ ] Optional patient accounts for booking history
- [ ] Telehealth video integration
- [ ] Automated waitlist management
- [ ] Multi-language support
- [ ] Mobile apps (iOS/Android)
- [ ] AI-powered appointment scheduling
- [ ] Automated session notes transcription
