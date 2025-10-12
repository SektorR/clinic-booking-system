package com.groundandgrow.repository;

import com.groundandgrow.model.GuestBooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestBookingRepository extends MongoRepository<GuestBooking, String> {

    Optional<GuestBooking> findByConfirmationToken(String token);

    List<GuestBooking> findByEmail(String email);

    List<GuestBooking> findByPsychologistId(String psychologistId);

    Optional<GuestBooking> findByStripeCheckoutSessionId(String sessionId);

    List<GuestBooking> findByPsychologistIdAndAppointmentDateTimeBetween(
            String psychologistId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
