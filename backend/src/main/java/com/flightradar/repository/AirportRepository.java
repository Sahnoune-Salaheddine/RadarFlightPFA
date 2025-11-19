package com.flightradar.repository;

import com.flightradar.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByCodeIATA(String codeIATA);
    Optional<Airport> findByName(String name);
}

