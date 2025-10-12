package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing Stripe checkout session information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponse {

    private String bookingId;
    private String sessionId;
    private String checkoutSessionId;
    private String checkoutUrl;
    private String confirmationToken;
}
