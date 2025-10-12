package com.groundandgrow.repository;

import com.groundandgrow.model.SessionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionTypeRepository extends MongoRepository<SessionType, String> {

    List<SessionType> findByModalityAndIsActive(SessionType.Modality modality, Boolean isActive);

    List<SessionType> findByIsActive(Boolean isActive);
}
