package com.flightradar.repository;

import com.flightradar.model.RadarCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RadarCenterRepository extends JpaRepository<RadarCenter, Long> {
    Optional<RadarCenter> findByCode(String code);
    Optional<RadarCenter> findByAirportId(Long airportId);
    List<RadarCenter> findAllByAirportId(Long airportId); // Pour gérer plusieurs résultats
    Optional<RadarCenter> findByUserId(Long userId);
}

