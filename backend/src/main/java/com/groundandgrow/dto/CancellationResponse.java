package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for booking cancellation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationResponse {

    private String bookingId;
    private boolean cancelled;
    private boolean refundEligible;
    private String message;
    private boolean refundProcessed;
    private Double refundAmount;
    private String refundStatus;
    private String refundError;
}
