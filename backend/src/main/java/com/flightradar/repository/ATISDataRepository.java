package com.flightradar.repository;

import com.flightradar.model.ATISData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ATISDataRepository extends JpaRepository<ATISData, Long> {
    Optional<ATISData> findFirstByAirportIdOrderByTimestampDesc(Long airportId);
}

