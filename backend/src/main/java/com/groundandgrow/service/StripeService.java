package com.groundandgrow.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe payment service for processing payments
 */
@Slf4j
@Service
public class StripeService {

    @Value("${stripe.api-key}")
    private String apiKey;

    @Value("${stripe.currency:AUD}")
    private String currency;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        log.info("Stripe initialized with currency: {}", currency);
    }

    /**
     * Create a Payment Intent for direct payment processing
     */
    public PaymentIntent createPaymentIntent(Long amount, String description, Map<String, String> metadata) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount) // Amount in cents
            .setCurrency(currency.toLowerCase())
            .setDescription(description)
            .putAllMetadata(metadata != null ? metadata : new HashMap<>())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        log.info("Payment Intent created: {}", paymentIntent.getId());
        return paymentIntent;
    }

    /**
     * Create a Checkout Session for hosted payment page
     */
    public Session createCheckoutSession(Long amount, String bookingId, String customerEmail) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .setCustomerEmail(customerEmail)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency.toLowerCase())
                            .setUnitAmount(amount) // Amount in cents
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Psychology Session - Ground & Grow Psychology")
                                    .setDescription("Professional counseling session")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("booking_id", bookingId)
            .build();

        Session session = Session.create(params);
        log.info("Checkout Session created: {}", session.getId());
        return session;
    }

    /**
     * Retrieve a Payment Intent
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * Retrieve a Checkout Session
     */
    public Session retrieveCheckoutSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    /**
     * Cancel a Payment Intent
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent cancelledIntent = paymentIntent.cancel();
        log.info("Payment Intent cancelled: {}", cancelledIntent.getId());
        return cancelledIntent;
    }

    /**
     * Create refund for a Payment Intent
     */
    public com.stripe.model.Refund createRefund(String paymentIntentId, Long amount) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("payment_intent", paymentIntentId);
        if (amount != null) {
            params.put("amount", amount); // Partial refund
        }

        com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
        log.info("Refund created: {}", refund.getId());
        return refund;
    }

    /**
     * Convert dollars to cents for Stripe
     */
    public Long convertToCents(Double dollars) {
        return Math.round(dollars * 100);
    }

    /**
     * Convert cents to dollars from Stripe
     */
    public Double convertToDollars(Long cents) {
        return cents / 100.0;
    }
}
