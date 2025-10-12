package com.groundandgrow.repository;

import com.groundandgrow.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

    Optional<Client> findByEmail(String email);

    List<Client> findByIsActive(Boolean isActive);
}
