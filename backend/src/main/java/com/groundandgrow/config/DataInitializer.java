package com.groundandgrow.config;

import com.groundandgrow.model.Availability;
import com.groundandgrow.model.Psychologist;
import com.groundandgrow.model.SessionType;
import com.groundandgrow.repository.AvailabilityRepository;
import com.groundandgrow.repository.PsychologistRepository;
import com.groundandgrow.repository.SessionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Initializes the database with sample data on application startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PsychologistRepository psychologistRepository;
    private final SessionTypeRepository sessionTypeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing database with sample data...");

        initializeSessionTypes();
        initializePsychologists();
        initializeAvailability();

        log.info("Database initialization completed!");
    }

    private void initializeSessionTypes() {
        if (sessionTypeRepository.count() > 0) {
            log.info("Session types already exist, skipping initialization");
            return;
        }

        log.info("Creating session types...");

        SessionType initialConsultation = SessionType.builder()
                .name("Initial Consultation")
                .description("First session to discuss your needs and goals")
                .durationMinutes(60)
                .price(new BigDecimal("150.00"))
                .modality(SessionType.Modality.ONLINE)
                .isActive(true)
                .build();

        SessionType standardSession = SessionType.builder()
                .name("Standard Session")
                .description("Regular therapy session")
                .durationMinutes(50)
                .price(new BigDecimal("120.00"))
                .modality(SessionType.Modality.ONLINE)
                .isActive(true)
                .build();

        SessionType inPersonSession = SessionType.builder()
                .name("In-Person Session")
                .description("Face-to-face therapy session at our clinic")
                .durationMinutes(50)
                .price(new BigDecimal("140.00"))
                .modality(SessionType.Modality.IN_PERSON)
                .isActive(true)
                .build();

        SessionType phoneSession = SessionType.builder()
                .name("Phone Consultation")
                .description("Therapy session via phone call")
                .durationMinutes(30)
                .price(new BigDecimal("80.00"))
                .modality(SessionType.Modality.PHONE)
                .isActive(true)
                .build();

        sessionTypeRepository.save(initialConsultation);
        sessionTypeRepository.save(standardSession);
        sessionTypeRepository.save(inPersonSession);
        sessionTypeRepository.save(phoneSession);

        log.info("Created {} session types", 4);
    }

    private void initializePsychologists() {
        if (psychologistRepository.count() > 0) {
            log.info("Psychologists already exist, skipping initialization");
            return;
        }

        log.info("Creating sample psychologists...");

        Psychologist dr1 = Psychologist.builder()
                .firstName("Sarah")
                .lastName("Thompson")
                .email("sarah.thompson@groundandgrow.com.au")
                .phone("+61 412 345 678")
                .specialization("Anxiety, Depression, Trauma")
                .registrationNumber("PSY0001234")
                .bio("Dr. Sarah Thompson is a registered psychologist with over 10 years of experience. She specializes in cognitive behavioral therapy (CBT) and has extensive experience working with anxiety, depression, and trauma-related conditions.")
                .password(passwordEncoder.encode("password123"))
                .role(Psychologist.Role.PSYCHOLOGIST)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Psychologist dr2 = Psychologist.builder()
                .firstName("Michael")
                .lastName("Chen")
                .email("michael.chen@groundandgrow.com.au")
                .phone("+61 423 456 789")
                .specialization("Relationship Issues, Stress Management, Work-Life Balance")
                .registrationNumber("PSY0005678")
                .bio("Michael Chen is a clinical psychologist with a focus on relationship counseling and stress management. He uses evidence-based approaches to help clients navigate life's challenges and build resilience.")
                .password(passwordEncoder.encode("password123"))
                .role(Psychologist.Role.PSYCHOLOGIST)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Psychologist dr3 = Psychologist.builder()
                .firstName("Emma")
                .lastName("Wilson")
                .email("emma.wilson@groundandgrow.com.au")
                .phone("+61 434 567 890")
                .specialization("Child & Adolescent Psychology, Family Therapy")
                .registrationNumber("PSY0009012")
                .bio("Emma Wilson specializes in working with children, adolescents, and families. With a warm and compassionate approach, she helps young people navigate emotional and behavioral challenges while supporting families through difficult times.")
                .password(passwordEncoder.encode("password123"))
                .role(Psychologist.Role.PSYCHOLOGIST)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        psychologistRepository.save(dr1);
        psychologistRepository.save(dr2);
        psychologistRepository.save(dr3);

        log.info("Created {} sample psychologists", 3);
        log.info("Psychologist login credentials - Email: [psychologist email], Password: password123");
    }

    private void initializeAvailability() {
        if (availabilityRepository.count() > 0) {
            log.info("Availability already exists, skipping initialization");
            return;
        }

        log.info("Creating default availability for psychologists...");

        // Get all psychologists
        List<Psychologist> psychologists = psychologistRepository.findAll();

        if (psychologists.isEmpty()) {
            log.warn("No psychologists found, skipping availability initialization");
            return;
        }

        int totalCreated = 0;

        for (Psychologist psychologist : psychologists) {
            // Create Monday-Friday availability, 9 AM to 5 PM
            for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
                Availability availability = Availability.builder()
                        .psychologistId(psychologist.getId())
                        .dayOfWeek(day)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .isRecurring(true)
                        .effectiveFrom(LocalDate.now())
                        .effectiveUntil(null) // No end date
                        .build();

                availabilityRepository.save(availability);
                totalCreated++;
            }
        }

        log.info("Created {} availability slots for {} psychologists", totalCreated, psychologists.size());
    }
}
