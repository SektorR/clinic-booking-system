package com.groundandgrow.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;

/**
 * Email service supporting both JavaMail and SendGrid
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.enabled:false}")
    private boolean sendGridEnabled;

    @Value("${app.notification.email.from}")
    private String fromEmail;

    @Value("${app.notification.email.from-name}")
    private String fromName;

    /**
     * Send simple text email using JavaMail
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email using JavaMail with Thymeleaf template
     */
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Templated email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send templated email to: {}", to, e);
            throw new RuntimeException("Failed to send templated email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email using SendGrid
     */
    public void sendEmailViaSendGrid(String to, String subject, String htmlContent) {
        if (!sendGridEnabled || sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.warn("SendGrid is not enabled or API key is missing");
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            log.info("SendGrid email sent. Status: {}, To: {}", response.getStatusCode(), to);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error: {}", response.getBody());
                throw new RuntimeException("SendGrid failed with status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("Failed to send email via SendGrid to: {}", to, e);
            throw new RuntimeException("Failed to send email via SendGrid", e);
        }
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmation(String to, String patientName, String psychologistName,
                                       String appointmentDate, String appointmentTime, String modality) {
        Map<String, Object> variables = Map.of(
            "patientName", patientName,
            "psychologistName", psychologistName,
            "appointmentDate", appointmentDate,
            "appointmentTime", appointmentTime,
            "modality", modality
        );

        sendTemplatedEmail(to, "Booking Confirmation - Ground & Grow Psychology", "booking-confirmation", variables);
    }

    /**
     * Send appointment reminder email
     */
    public void sendAppointmentReminder(String to, String patientName, String psychologistName,
                                       String appointmentDate, String appointmentTime, String modality) {
        Map<String, Object> variables = Map.of(
            "patientName", patientName,
            "psychologistName", psychologistName,
            "appointmentDate", appointmentDate,
            "appointmentTime", appointmentTime,
            "modality", modality
        );

        sendTemplatedEmail(to, "Appointment Reminder - Ground & Grow Psychology", "appointment-reminder", variables);
    }

    /**
     * Send cancellation confirmation email
     */
    public void sendCancellationConfirmation(String to, String patientName, String appointmentDate) {
        Map<String, Object> variables = Map.of(
            "patientName", patientName,
            "appointmentDate", appointmentDate
        );

        sendTemplatedEmail(to, "Appointment Cancelled - Ground & Grow Psychology", "cancellation-confirmation", variables);
    }
}
