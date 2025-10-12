package com.groundandgrow.repository;

import com.groundandgrow.model.Psychologist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologistRepository extends MongoRepository<Psychologist, String> {

    Optional<Psychologist> findByEmail(String email);

    List<Psychologist> findByIsActive(Boolean isActive);
}
