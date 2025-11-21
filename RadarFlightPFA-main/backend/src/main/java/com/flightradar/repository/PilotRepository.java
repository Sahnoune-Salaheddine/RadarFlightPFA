package com.flightradar.repository;

import com.flightradar.model.Pilot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PilotRepository extends JpaRepository<Pilot, Long> {
    Optional<Pilot> findByLicense(String license);
    
    /**
     * Trouve un pilote par son user_id
     * Utilise une requête explicite pour garantir un résultat unique
     */
    @Query("SELECT p FROM Pilot p WHERE p.user.id = :userId")
    Optional<Pilot> findByUserId(@Param("userId") Long userId);
}

