package com.flightradar.repository;

import com.flightradar.model.Flight;
import com.flightradar.model.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findByAircraftId(Long aircraftId);
    List<Flight> findByDepartureAirportId(Long airportId);
    List<Flight> findByArrivalAirportId(Long airportId);
    List<Flight> findByFlightStatus(FlightStatus status);
    Optional<Flight> findByAircraftIdAndFlightStatusNot(Long aircraftId, FlightStatus status);
}

