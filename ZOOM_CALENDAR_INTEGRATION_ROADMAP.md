# Zoom & Google Calendar Integration - High Priority Feature

**Feature ID:** FEATURE-001
**Priority:** P1 (High)
**Status:** Planned
**Timeline:** 2-3 weeks
**Last Updated:** 2025-10-05

---

## Overview

Integrate Zoom video conferencing and Google Calendar event management into the appointment booking flow to provide seamless virtual meeting scheduling and calendar synchronization for both patients and psychologists.

---

## Business Value

- **Automated Meeting Links:** No manual Zoom link creation needed
- **Calendar Sync:** Automatic calendar events for both parties
- **Reduced No-Shows:** Calendar reminders increase attendance
- **Professional Experience:** Seamless integration improves patient satisfaction
- **Time Savings:** 5-10 minutes saved per appointment setup

---

## Architecture Overview

```
Appointment Lifecycle Integration:
┌─────────────────────────────────────────────────────────────┐
│ 1. Booking Created                                          │
│    └→ Generate Zoom Meeting Link                            │
│    └→ Create Google Calendar Event                          │
│    └→ Send Email with Both Links                            │
├─────────────────────────────────────────────────────────────┤
│ 2. Booking Confirmed                                        │
│    └→ Send Calendar Invite to Both Parties                  │
│    └→ Update Calendar Event Status                          │
├─────────────────────────────────────────────────────────────┤
│ 3. Booking Rescheduled                                      │
│    └→ Update Zoom Meeting Time                              │
│    └→ Update Calendar Event                                 │
│    └→ Send Updated Invites                                  │
├─────────────────────────────────────────────────────────────┤
│ 4. Booking Cancelled                                        │
│    └→ End/Delete Zoom Meeting                               │
│    └→ Cancel Calendar Event                                 │
│    └→ Send Cancellation Notifications                       │
├─────────────────────────────────────────────────────────────┤
│ 5. Pre-Appointment Reminder (24h before)                    │
│    └→ Send Email with Zoom Link                             │
│    └→ Send SMS Reminder (optional)                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Database & Model Updates (Week 1, Days 1-2)

### P1 Tasks - Database Schema

- [ ] **Update GuestBooking Model**
  - Add `zoomMeetingId` (String) - Zoom meeting identifier
  - Add `zoomJoinUrl` (String) - Patient join link
  - Add `zoomStartUrl` (String) - Psychologist start link (host)
  - Add `zoomPassword` (String, encrypted) - Meeting password
  - Add `googleCalendarEventId` (String) - Calendar event ID
  - Add `googleCalendarEventLink` (String) - HTML link to event
  - Add `psychologistCalendarEventId` (String) - Psychologist's calendar event
  - Add `calendarSyncStatus` (Enum: synced, pending, failed)
  - Add `lastSyncedAt` (LocalDateTime) - Last sync timestamp
  - Add `meetingCreatedAt` (LocalDateTime) - When meeting was created

- [ ] **Create Zoom Configuration Collection**
  ```java
  // Store Zoom settings per psychologist (future multi-account support)
  @Document(collection = "zoom_config")
  public class ZoomConfig {
      private String id;
      private String psychologistId;
      private String zoomAccountId;
      private String zoomUserId;
      private Boolean enabled = true;
      private LocalDateTime createdAt;
  }
  ```

- [ ] **Create Calendar Sync Log Collection**
  ```java
  // Track sync attempts for debugging
  @Document(collection = "calendar_sync_log")
  public class CalendarSyncLog {
      private String id;
      private String bookingId;
      private String action; // create, update, delete
      private String status; // success, failed
      private String errorMessage;
      private LocalDateTime attemptedAt;
  }
  ```

### P1 Tasks - DTOs

- [ ] **Create ZoomMeetingRequest DTO**
- [ ] **Create ZoomMeetingResponse DTO**
- [ ] **Create ZoomTokenResponse DTO**
- [ ] **Create CalendarEventRequest DTO**
- [ ] **Create CalendarEventResponse DTO**
- [ ] **Update AppointmentDTO** to include meeting links

---

## Phase 2: Zoom Integration (Week 1, Days 3-5)

### Dependencies Required

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

### P1 Tasks - Configuration

- [ ] **Create application.properties entries**
  ```properties
  # Zoom Configuration
  zoom.api.base-url=https://api.zoom.us/v2
  zoom.oauth.account-id=${ZOOM_ACCOUNT_ID:}
  zoom.oauth.client-id=${ZOOM_CLIENT_ID:}
  zoom.oauth.client-secret=${ZOOM_CLIENT_SECRET:}
  zoom.meeting.default-duration=50
  zoom.meeting.waiting-room=true
  zoom.meeting.join-before-host=false
  zoom.meeting.mute-upon-entry=true
  zoom.meeting.auto-recording=none
  zoom.enabled=${ZOOM_ENABLED:false}
  ```

- [ ] **Create ZoomConfig.java**
  - Load configuration from properties
  - Validate required fields
  - Handle enabled/disabled state

### P1 Tasks - Zoom Service Implementation

- [ ] **Create ZoomService.java**
  - `getServerToServerToken()` - OAuth token generation
  - `createMeeting(GuestBooking, Psychologist)` - Create Zoom meeting
  - `updateMeeting(String meetingId, GuestBooking)` - Update meeting time
  - `deleteMeeting(String meetingId)` - Delete/end meeting
  - `getMeetingDetails(String meetingId)` - Get meeting info
  - Error handling and retry logic

- [ ] **Create ZoomMeetingRequest Model**
  ```java
  @Data
  @Builder
  public class ZoomMeetingRequest {
      private String topic;
      private Integer type; // 2 = scheduled
      private String startTime; // ISO 8601
      private Integer duration; // minutes
      private String timezone;
      private ZoomMeetingSettings settings;
  }
  ```

- [ ] **Create ZoomMeetingSettings Model**
  ```java
  @Data
  @Builder
  public class ZoomMeetingSettings {
      private Boolean waitingRoom;
      private Boolean joinBeforeHost;
      private Boolean muteUponEntry;
      private String autoRecording; // none, cloud, local
      private Boolean participantVideo;
      private Boolean hostVideo;
  }
  ```

- [ ] **Create ZoomMeetingResponse Model**
  ```java
  @Data
  public class ZoomMeetingResponse {
      private Long id;
      private String uuid;
      private String joinUrl;
      private String startUrl;
      private String password;
      private String h323Password;
      private String encryptedPassword;
      private ZoomMeetingSettings settings;
  }
  ```

### P1 Tasks - Security

- [ ] **Create MeetingSecurityService.java**
  - Encrypt Zoom passwords before storing
  - Decrypt when needed
  - Use AES-256 encryption
  - Store encryption key in environment variable

---

## Phase 3: Google Calendar Integration (Week 2, Days 1-3)

### Dependencies Required

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-calendar</artifactId>
    <version>v3-rev20231123-2.0.0</version>
</dependency>
<dependency>
    <groupId>com.google.auth</groupId>
    <artifactId>google-auth-library-oauth2-http</artifactId>
    <version>1.19.0</version>
</dependency>
```

### P1 Tasks - Google Cloud Setup

- [ ] **Create Google Cloud Project**
  - Enable Google Calendar API
  - Create Service Account
  - Download service account credentials JSON
  - Grant calendar permissions to service account

- [ ] **Configure Google Calendar Properties**
  ```properties
  # Google Calendar Configuration
  google.calendar.credentials-path=${GOOGLE_CALENDAR_CREDENTIALS_PATH:}
  google.calendar.application-name=Ground & Grow Psychology
  google.calendar.enabled=${GOOGLE_CALENDAR_ENABLED:false}
  google.calendar.send-invites=true
  google.calendar.reminder-minutes=60,1440
  ```

### P1 Tasks - Calendar Service Implementation

- [ ] **Create GoogleCalendarService.java**
  - `getCalendarService()` - Initialize Google Calendar API client
  - `createCalendarEvent(GuestBooking, Psychologist, zoomUrl)` - Create event
  - `updateCalendarEvent(String eventId, GuestBooking)` - Update event
  - `deleteCalendarEvent(String eventId)` - Delete/cancel event
  - `addAttendee(String eventId, String email)` - Add attendee
  - `buildEventDescription()` - Format event description
  - Error handling and retry logic

- [ ] **Create GoogleCalendarConfig.java**
  - Load credentials from JSON file
  - Configure OAuth scopes
  - Handle authentication

- [ ] **Create CalendarEventResponse DTO**
  ```java
  @Data
  @Builder
  public class CalendarEventResponse {
      private String eventId;
      private String htmlLink;
      private String hangoutLink;
      private String status;
      private List<String> attendeeEmails;
  }
  ```

### P1 Tasks - Event Template

- [ ] **Design Calendar Event Format**
  ```
  Title: Therapy Session - [Psychologist Name]

  Description:
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Ground & Grow Psychology
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Psychologist: Dr. [Name]
  Patient: [First Name] [Last Initial].
  Duration: [X] minutes
  Session Type: [Type]
  Modality: [Video/In-Person]

  📹 Zoom Meeting Link:
  [Zoom Join URL]

  Meeting ID: [ID]
  Password: [Password]

  ⏰ Please join 5 minutes early

  📞 Need to reschedule?
  Contact us at least 24 hours in advance
  Phone: +61 XXX XXX XXX
  Email: admin@groundandgrow.com.au

  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ```

---

## Phase 4: Enhanced Booking Service (Week 2, Days 4-5)

### P1 Tasks - Service Layer Integration

- [ ] **Update BookingService.java**
  - Inject ZoomService and GoogleCalendarService
  - Add `createBookingWithIntegrations()` method
  - Add `rescheduleAppointmentWithIntegrations()` method
  - Add `cancelAppointmentWithCleanup()` method
  - Add transaction management
  - Add rollback handling if integration fails

- [ ] **Create IntegrationOrchestrator.java**
  ```java
  @Service
  public class IntegrationOrchestrator {

      @Transactional
      public BookingIntegrationResult createFullBooking(BookingRequest request) {
          // 1. Create booking record
          // 2. Create Zoom meeting
          // 3. Create calendar event
          // 4. Send confirmation emails
          // 5. Handle partial failures gracefully
      }
  }
  ```

- [ ] **Add Integration Status Tracking**
  - Log each integration step
  - Store success/failure status
  - Enable manual retry for failed integrations

### P1 Tasks - Error Handling

- [ ] **Create custom exceptions**
  - `ZoomIntegrationException`
  - `CalendarIntegrationException`
  - `MeetingCreationException`

- [ ] **Implement retry logic**
  - Retry failed Zoom API calls (max 3 attempts)
  - Retry failed Calendar API calls (max 3 attempts)
  - Exponential backoff strategy

- [ ] **Graceful degradation**
  - Allow booking to succeed even if integrations fail
  - Mark integration status as "pending retry"
  - Queue failed integrations for background retry

---

## Phase 5: API Endpoints (Week 3, Days 1-2)

### P1 Tasks - Controller Updates

- [ ] **Update BookingController.java**
  - POST `/api/bookings` - Include integration in response
  - PUT `/api/bookings/{id}/reschedule` - Update all integrations
  - DELETE `/api/bookings/{id}` - Cleanup integrations

- [ ] **Update PsychologistController.java**
  - GET `/api/psychologist/appointments/{id}/meeting-link` - Get Zoom link
  - POST `/api/psychologist/appointments/{id}/regenerate-meeting` - Regenerate if expired
  - GET `/api/psychologist/appointments/{id}/calendar-event` - Get calendar details

- [ ] **Create new endpoints**
  - POST `/api/integrations/zoom/test` - Test Zoom connection (admin)
  - POST `/api/integrations/calendar/test` - Test Calendar connection (admin)
  - GET `/api/integrations/status` - Check integration health

### P1 Tasks - Response DTOs

- [ ] **Update AppointmentDTO**
  ```java
  // Add to AppointmentDTO.java
  private String zoomJoinUrl;
  private String googleCalendarLink;
  private String meetingPassword;
  private Boolean hasActiveZoomMeeting;
  private Boolean hasCalendarEvent;
  private String calendarSyncStatus;
  ```

---

## Phase 6: Email Notifications (Week 3, Day 3)

### P1 Tasks - Email Templates

- [ ] **Update booking confirmation email**
  - Include Zoom meeting link prominently
  - Add "Add to Calendar" button (Google Calendar URL)
  - Include .ics file attachment
  - Show meeting password

- [ ] **Create reminder email (24h before)**
  ```
  Subject: Reminder: Your therapy session tomorrow at [TIME]

  Your appointment is tomorrow!

  📅 Date: [Date]
  ⏰ Time: [Time]
  👤 Psychologist: Dr. [Name]

  📹 Join Zoom Meeting:
  [Large Button: Join Meeting]

  Meeting ID: [ID]
  Password: [Password]

  ⏰ Please join 5 minutes early to test your connection.
  ```

- [ ] **Update reschedule email template**
- [ ] **Update cancellation email template**

### P1 Tasks - Calendar File Generation

- [ ] **Create ICS file generator**
  ```java
  public class ICSGenerator {
      public byte[] generateICS(GuestBooking booking) {
          // Generate RFC 5545 compliant .ics file
      }
  }
  ```

---

## Phase 7: Frontend Integration (Week 3, Days 4-5)

### P1 Tasks - Booking Confirmation Page

- [ ] **Update BookingSuccessPage.tsx**
  - Display Zoom meeting link prominently
  - Add "Add to Google Calendar" button
  - Add "Add to Apple Calendar" button (.ics download)
  - Add "Copy Meeting Link" button
  - Show meeting password with copy button
  - Display calendar event status

### P1 Tasks - Psychologist Dashboard

- [ ] **Update DashboardPage.tsx**
  - Add "Join Meeting" button for upcoming appointments
  - Show meeting link 10 minutes before appointment
  - Display calendar sync status indicator
  - Add quick access to meeting links

- [ ] **Update AppointmentsPage.tsx**
  - Add Zoom link column
  - Add "Copy Meeting Link" action
  - Add calendar sync status badge
  - Add "Regenerate Meeting" action for admins

### P1 Tasks - Utility Functions

- [ ] **Create calendarUtils.ts**
  ```typescript
  export const generateGoogleCalendarUrl = (appointment: AppointmentDTO) => {
      // Generate Google Calendar "Add Event" URL
  }

  export const generateICSFile = (appointment: AppointmentDTO) => {
      // Generate .ics file blob for download
  }

  export const copyMeetingLink = (zoomUrl: string) => {
      // Copy to clipboard with notification
  }
  ```

---

## Phase 8: Testing & Quality Assurance (Ongoing)

### P1 Tasks - Unit Tests

- [ ] **ZoomService Tests**
  - Test token generation
  - Test meeting creation
  - Test meeting update
  - Test meeting deletion
  - Test error handling

- [ ] **GoogleCalendarService Tests**
  - Test event creation
  - Test event update
  - Test event deletion
  - Test attendee management

- [ ] **IntegrationOrchestrator Tests**
  - Test full booking flow
  - Test partial failure scenarios
  - Test rollback logic

### P1 Tasks - Integration Tests

- [ ] **End-to-end booking flow test**
  - Create booking → Verify Zoom meeting created
  - Create booking → Verify Calendar event created
  - Reschedule → Verify both updated
  - Cancel → Verify both deleted

### P1 Tasks - Manual Testing

- [ ] Test with real Zoom account (sandbox)
- [ ] Test with real Google Calendar
- [ ] Verify email templates render correctly
- [ ] Test on mobile devices
- [ ] Test calendar app integrations (Outlook, Apple Calendar)

---

## Configuration Examples

### Development Environment (.env)

```bash
# Zoom Integration (Development)
ZOOM_ENABLED=true
ZOOM_ACCOUNT_ID=your_zoom_account_id
ZOOM_CLIENT_ID=your_zoom_client_id
ZOOM_CLIENT_SECRET=your_zoom_client_secret

# Google Calendar Integration (Development)
GOOGLE_CALENDAR_ENABLED=true
GOOGLE_CALENDAR_CREDENTIALS_PATH=/path/to/service-account-credentials.json

# Security
MEETING_PASSWORD_ENCRYPTION_KEY=your-256-bit-encryption-key-change-in-prod
```

### Production Checklist

- [ ] Use production Zoom account
- [ ] Use production Google Cloud project
- [ ] Enable calendar encryption
- [ ] Configure webhook endpoints for Zoom events
- [ ] Set up monitoring and alerting
- [ ] Test calendar invite spam filters
- [ ] Verify HIPAA compliance (disable recording, transcripts)
- [ ] Configure auto-meeting-deletion after 30 days

---

## Security & Privacy Considerations

### HIPAA/Privacy Compliance

- [ ] **Disable Zoom Features**
  - ❌ Auto-recording (cloud or local)
  - ❌ Meeting transcripts
  - ❌ Waiting room screenshots
  - ❌ Chat logs
  - ❌ Breakout rooms
  - ✅ End-to-end encryption (E2EE)
  - ✅ Waiting room enabled
  - ✅ Password protection

- [ ] **Data Retention**
  - Auto-delete Zoom meetings 24h after end time
  - Remove meeting links from database after 30 days
  - Keep calendar sync logs for 90 days only

- [ ] **Access Control**
  - Only psychologist gets "start" link (host privileges)
  - Patient gets "join" link (participant only)
  - Encrypt meeting passwords in database
  - Use HTTPS for all meeting links

### Encryption

```java
@Service
public class MeetingSecurityService {

    @Value("${meeting.password.encryption.key}")
    private String encryptionKey;

    public String encryptZoomPassword(String password) {
        // AES-256-GCM encryption
        // Store IV with encrypted data
    }

    public String decryptZoomPassword(String encryptedPassword) {
        // Decrypt when sending to patient/psychologist
    }
}
```

---

## Monitoring & Alerts

### Metrics to Track

- [ ] Zoom meeting creation success rate
- [ ] Calendar event creation success rate
- [ ] Integration failure rate
- [ ] Average API response time
- [ ] Failed integration retry count
- [ ] Meeting link generation time

### Alerts to Configure

- [ ] Alert if Zoom API rate limit approached
- [ ] Alert if Calendar sync fails > 5 times
- [ ] Alert if OAuth token refresh fails
- [ ] Alert if meeting creation takes > 10 seconds

---

## Rollout Strategy

### Phase A: Internal Testing (Week 1)
- Deploy to development environment
- Test with internal staff appointments
- Verify all integrations work correctly

### Phase B: Beta Testing (Week 2)
- Enable for 10% of new bookings
- Monitor error rates
- Collect feedback from psychologists
- Fix critical bugs

### Phase C: Full Rollout (Week 3)
- Enable for 100% of bookings
- Monitor closely for first week
- Have rollback plan ready

---

## Success Metrics

- ✅ 95%+ Zoom meeting creation success rate
- ✅ 90%+ Calendar sync success rate
- ✅ < 5 seconds average meeting generation time
- ✅ Zero patient complaints about missing meeting links
- ✅ 20% reduction in appointment no-shows (via calendar reminders)
- ✅ 100% HIPAA compliance for video sessions

---

## Future Enhancements (Post-Launch)

### P2 (Medium Priority)

- [ ] **Psychologist Calendar Availability Sync**
  - Sync psychologist's Google Calendar to show availability
  - Block out personal appointments automatically
  - Two-way sync

- [ ] **Zoom Webhook Integration**
  - Listen for meeting start/end events
  - Track actual session duration
  - Update appointment status automatically

- [ ] **Multiple Calendar Support**
  - Office 365/Outlook integration
  - Apple iCloud Calendar integration
  - CalDAV support

- [ ] **Advanced Meeting Features**
  - Scheduled breakout rooms for group therapy
  - Co-host support for supervision sessions
  - Custom waiting room branding

### P3 (Low Priority)

- [ ] Meeting recordings management (opt-in only, with consent)
- [ ] Post-session survey sent via Zoom chat
- [ ] Integration with practice management systems
- [ ] Automated meeting link rotation for security

---

## Dependencies & Prerequisites

### Required Services

1. **Zoom Account**
   - Type: Pro or higher
   - Features: API access, Server-to-Server OAuth
   - Cost: ~$15-20/month per host

2. **Google Cloud Account**
   - Type: Free tier available
   - Features: Calendar API, Service Account
   - Cost: Free for normal usage

### Development Tools

- Java 17+
- Spring Boot 3.2.0
- Maven 3.8+
- MongoDB 6.0+
- Postman (for API testing)

---

## Documentation Requirements

- [ ] **API Documentation**
  - Add Swagger annotations for new endpoints
  - Document Zoom integration flow
  - Document Calendar integration flow

- [ ] **User Guide**
  - How to join a Zoom meeting
  - How to add appointment to calendar
  - Troubleshooting guide

- [ ] **Admin Guide**
  - How to configure Zoom credentials
  - How to configure Google Calendar
  - How to troubleshoot failed integrations
  - How to manually regenerate meeting links

---

## Risk Assessment

### High Risk
- **Zoom API downtime** - Mitigation: Graceful degradation, allow booking without meeting link
- **Google Calendar quota limits** - Mitigation: Implement rate limiting, request quota increase
- **OAuth token expiration** - Mitigation: Auto-refresh tokens, alert on failures

### Medium Risk
- **Meeting link security** - Mitigation: Password protection, waiting room, encryption
- **Calendar spam filters** - Mitigation: SPF/DKIM setup, test email deliverability
- **Time zone handling** - Mitigation: Store all times in UTC, convert for display

### Low Risk
- **Feature adoption** - Mitigation: User training, clear documentation
- **Cost overruns** - Mitigation: Monitor API usage, set up billing alerts

---

## Timeline Summary

| Week | Phase | Deliverables |
|------|-------|--------------|
| Week 1, Days 1-2 | Database & Models | Updated schemas, DTOs |
| Week 1, Days 3-5 | Zoom Integration | ZoomService, OAuth, meeting creation |
| Week 2, Days 1-3 | Google Calendar | GoogleCalendarService, event creation |
| Week 2, Days 4-5 | Enhanced Booking | IntegrationOrchestrator, error handling |
| Week 3, Days 1-2 | API Endpoints | Controller updates, new endpoints |
| Week 3, Day 3 | Email Notifications | Updated templates, .ics generation |
| Week 3, Days 4-5 | Frontend Integration | UI updates, calendar buttons |
| Ongoing | Testing & QA | Unit tests, integration tests, UAT |

**Total Estimated Time:** 15-18 working days (3 weeks)

---

## Notes

- This feature is **HIGH PRIORITY** and should be started after the psychologist dashboard is confirmed working
- All code should follow existing project patterns and conventions
- Security and privacy are paramount - no shortcuts
- Focus on reliability over features
- Plan for graceful degradation if external services fail

---

**Status:** ✅ Ready to Start
**Assigned To:** [To be assigned]
**Blocked By:** Psychologist dashboard fix (TASK-001)
