package com.groundandgrow.repository;

import com.groundandgrow.model.Availability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilityRepository extends MongoRepository<Availability, String> {

    List<Availability> findByPsychologistId(String psychologistId);

    List<Availability> findByPsychologistIdAndDayOfWeek(String psychologistId, DayOfWeek dayOfWeek);
}
