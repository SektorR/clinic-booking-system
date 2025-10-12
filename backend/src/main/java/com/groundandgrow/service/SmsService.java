package com.groundandgrow.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * SMS service using Twilio
 */
@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @PostConstruct
    public void init() {
        if (twilioEnabled && accountSid != null && !accountSid.isEmpty()
            && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio is not enabled or credentials are missing");
        }
    }

    /**
     * Send SMS message
     */
    public void sendSms(String toPhoneNumber, String messageBody) {
        if (!twilioEnabled) {
            log.warn("Twilio is disabled. SMS not sent to: {}", toPhoneNumber);
            return;
        }

        try {
            // Ensure phone number is in E.164 format (e.g., +61412345678 for Australia)
            String formattedTo = formatAustralianPhoneNumber(toPhoneNumber);

            Message message = Message.creator(
                new PhoneNumber(formattedTo),
                new PhoneNumber(fromPhoneNumber),
                messageBody
            ).create();

            log.info("SMS sent successfully. SID: {}, To: {}", message.getSid(), toPhoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toPhoneNumber, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    /**
     * Format Australian phone number to E.164 format
     */
    private String formatAustralianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        // Handle Australian mobile numbers
        if (digitsOnly.startsWith("04")) {
            // 04XX XXX XXX -> +614XX XXX XXX
            return "+61" + digitsOnly.substring(1);
        } else if (digitsOnly.startsWith("614")) {
            // Already in correct format without +
            return "+" + digitsOnly;
        } else if (digitsOnly.startsWith("+614")) {
            // Already in correct format
            return digitsOnly;
        }

        // If already starts with +, return as is
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        // Default: assume it's an Australian number
        return "+61" + digitsOnly;
    }

    /**
     * Send booking confirmation SMS
     */
    public void sendBookingConfirmationSms(String toPhoneNumber, String patientName,
                                          String psychologistName, String appointmentDate, String appointmentTime) {
        String message = String.format(
            "Hi %s, your appointment with %s is confirmed for %s at %s. Ground & Grow Psychology",
            patientName, psychologistName, appointmentDate, appointmentTime
        );
        sendSms(toPhoneNumber, message);
    }

    /**
     * Send appointment reminder SMS
     */
    public void sendAppointmentReminderSms(String toPhoneNumber, String patientName,
                                          String psychologistName, String appointmentDate, String appointmentTime) {
        String message = String.format(
            "Reminder: %s, your appointment with %s is tomorrow at %s. Ground & Grow Psychology",
            patientName, psychologistName, appointmentTime
        );
        sendSms(toPhoneNumber, message);
    }

    /**
     * Send cancellation confirmation SMS
     */
    public void sendCancellationSms(String toPhoneNumber, String patientName, String appointmentDate) {
        String message = String.format(
            "Hi %s, your appointment on %s has been cancelled. Contact us to reschedule. Ground & Grow Psychology",
            patientName, appointmentDate
        );
        sendSms(toPhoneNumber, message);
    }

    /**
     * Send verification code SMS
     */
    public void sendVerificationCode(String toPhoneNumber, String code) {
        String message = String.format(
            "Your Ground & Grow Psychology verification code is: %s. Valid for 10 minutes.",
            code
        );
        sendSms(toPhoneNumber, message);
    }
}
