package com.flightradar.repository;

import com.flightradar.model.Runway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunwayRepository extends JpaRepository<Runway, Long> {
    List<Runway> findByAirportId(Long airportId);
}

