package com.groundandgrow.repository;

import com.groundandgrow.model.TimeOff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeOffRepository extends MongoRepository<TimeOff, String> {

    List<TimeOff> findByPsychologistId(String psychologistId);
}
