package com.groundandgrow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // COMPLETED, NO_SHOW, CANCELLED

    private String reason;
}
