package com.flightradar.repository;

import com.flightradar.model.Pilot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PilotRepository extends JpaRepository<Pilot, Long> {
    Optional<Pilot> findByLicense(String license);
    Optional<Pilot> findByUserId(Long userId);
}

