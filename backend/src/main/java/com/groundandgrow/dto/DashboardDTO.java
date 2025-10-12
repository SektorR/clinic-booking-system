package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private PsychologistDTO psychologist;
    private List<AppointmentDTO> todayAppointments;
    private List<AppointmentDTO> upcomingAppointments;
    private DashboardStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private Long totalSessions;
        private Long pendingBookings;
        private Long completedThisWeek;
        private Long completedThisMonth;
        private Long cancelledThisMonth;
        private Long noShowsThisMonth;
        private Integer unreadMessages;
    }
}
