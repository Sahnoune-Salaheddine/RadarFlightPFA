package com.flightradar.repository;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistration(String registration);
    List<Aircraft> findByAirportId(Long airportId);
    List<Aircraft> findByStatus(AircraftStatus status);
    List<Aircraft> findByPilotId(Long pilotId);
}

