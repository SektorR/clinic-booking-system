package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSummaryDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer totalAppointments;
    private Integer completedAppointments;
    private Integer cancelledAppointments;
    private LocalDateTime lastAppointmentDate;
    private LocalDateTime nextAppointmentDate;
    private Integer unreadMessages;
}
