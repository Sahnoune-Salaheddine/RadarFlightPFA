package com.flightradar.repository;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistration(String registration);
    List<Aircraft> findByAirportId(Long airportId);
    List<Aircraft> findByStatus(AircraftStatus status);
    
    /**
     * Trouve l'avion assigné à un pilote (relation OneToOne)
     * Retourne Optional car il ne devrait y avoir qu'un seul avion par pilote
     */
    @Query("SELECT a FROM Aircraft a WHERE a.pilot.id = :pilotId")
    Optional<Aircraft> findByPilotId(@Param("pilotId") Long pilotId);
    
    /**
     * Trouve l'avion assigné à un pilote par username
     */
    @Query("SELECT a FROM Aircraft a WHERE a.pilot.user.username = :username")
    Optional<Aircraft> findByPilotUsername(@Param("username") String username);
    
    /**
     * Trouve les avions non assignés (sans pilote)
     */
    @Query("SELECT a FROM Aircraft a WHERE a.pilot IS NULL")
    List<Aircraft> findUnassignedAircraft();
}

