package com.groundandgrow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session_types")
public class SessionType {

    @Id
    private String id;

    private String name;

    private String description;

    private Integer durationMinutes;

    private BigDecimal price; // AUD

    private Modality modality;

    @Builder.Default
    private Boolean isActive = true;

    public enum Modality {
        ONLINE,
        IN_PERSON,
        PHONE
    }
}
