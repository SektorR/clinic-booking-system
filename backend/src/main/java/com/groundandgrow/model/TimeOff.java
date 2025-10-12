package com.groundandgrow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "time_off")
public class TimeOff {

    @Id
    private String id;

    @Indexed
    private String psychologistId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String reason;

    @CreatedDate
    private LocalDateTime createdAt;
}
