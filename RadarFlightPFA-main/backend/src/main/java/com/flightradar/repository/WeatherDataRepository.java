package com.flightradar.repository;

import com.flightradar.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    Optional<WeatherData> findFirstByAirportIdOrderByTimestampDesc(Long airportId);
    List<WeatherData> findByAirportId(Long airportId);
    List<WeatherData> findByAlertTrue();
    List<WeatherData> findByAirportIdOrderByTimestampDesc(Long airportId);
}

