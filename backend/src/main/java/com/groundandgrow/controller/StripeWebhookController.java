package com.groundandgrow.controller;

import com.groundandgrow.service.GuestBookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Stripe webhook events
 */
@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final GuestBookingService guestBookingService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Handle Stripe webhook events
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {

        log.info("Received Stripe webhook");

        Event event;

        // Verify webhook signature
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        log.info("Webhook event type: {}", event.getType());

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "checkout.session.expired":
                handleCheckoutSessionExpired(event);
                break;

            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Handle checkout.session.completed event
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                log.error("Deserialization failed for checkout.session.completed");
                return;
            }

            Session session = (Session) stripeObject;
            log.info("Checkout session completed: {}", session.getId());

            // Get booking ID from metadata
            String bookingId = session.getMetadata().get("booking_id");
            log.info("Processing payment success for booking: {}", bookingId);

            // Update booking and send confirmations
            guestBookingService.handlePaymentSuccess(session.getId());

        } catch (Exception e) {
            log.error("Error handling checkout.session.completed", e);
        }
    }

    /**
     * Handle checkout.session.expired event
     */
    private void handleCheckoutSessionExpired(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                log.error("Deserialization failed for checkout.session.expired");
                return;
            }

            Session session = (Session) stripeObject;
            log.info("Checkout session expired: {}", session.getId());

            // Mark booking as failed
            guestBookingService.handlePaymentFailure(session.getId());

        } catch (Exception e) {
            log.error("Error handling checkout.session.expired", e);
        }
    }

    /**
     * Handle payment_intent.succeeded event
     */
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            log.info("Payment intent succeeded");
            // Additional handling if needed
        } catch (Exception e) {
            log.error("Error handling payment_intent.succeeded", e);
        }
    }

    /**
     * Handle payment_intent.payment_failed event
     */
    private void handlePaymentIntentFailed(Event event) {
        try {
            log.info("Payment intent failed");
            // Additional handling if needed
        } catch (Exception e) {
            log.error("Error handling payment_intent.payment_failed", e);
        }
    }

    /**
     * Handle charge.refunded event
     */
    private void handleChargeRefunded(Event event) {
        try {
            log.info("Charge refunded");
            // Additional handling if needed (already handled in cancelBooking)
        } catch (Exception e) {
            log.error("Error handling charge.refunded", e);
        }
    }
}
