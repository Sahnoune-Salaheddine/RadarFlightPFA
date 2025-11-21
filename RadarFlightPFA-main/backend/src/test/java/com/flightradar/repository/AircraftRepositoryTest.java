package com.flightradar.repository;

import com.flightradar.model.Aircraft;
import com.flightradar.model.Pilot;
import com.flightradar.model.User;
import com.flightradar.model.Role;
import com.flightradar.model.AircraftStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AircraftRepository
 * Vérifie que la relation OneToOne fonctionne correctement
 */
@DataJpaTest
class AircraftRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Test
    void testFindByPilotId_OneToOne() {
        // Arrange
        User user = new User();
        user.setUsername("test_pilot");
        user.setPassword("password");
        user.setRole(Role.PILOTE);
        user = entityManager.persistAndFlush(user);

        Pilot pilot = new Pilot();
        pilot.setName("Test Pilot");
        pilot.setLicense("TEST1");
        pilot.setUser(user);
        pilot = entityManager.persistAndFlush(pilot);

        Aircraft aircraft = new Aircraft();
        aircraft.setRegistration("CN-TEST");
        aircraft.setModel("A320");
        aircraft.setStatus(AircraftStatus.AU_SOL);
        aircraft.setPilot(pilot);
        aircraft = entityManager.persistAndFlush(aircraft);

        // Act
        Optional<Aircraft> result = aircraftRepository.findByPilotId(pilot.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(aircraft.getId(), result.get().getId());
        assertEquals(pilot.getId(), result.get().getPilot().getId());
        // Vérifier qu'un seul résultat est retourné (pas de liste)
        assertNotNull(result.get());
    }

    @Test
    void testFindByPilotId_NoAircraft() {
        // Arrange
        User user = new User();
        user.setUsername("test_pilot2");
        user.setPassword("password");
        user.setRole(Role.PILOTE);
        user = entityManager.persistAndFlush(user);

        Pilot pilot = new Pilot();
        pilot.setName("Test Pilot 2");
        pilot.setLicense("TEST2");
        pilot.setUser(user);
        pilot = entityManager.persistAndFlush(pilot);

        // Act
        Optional<Aircraft> result = aircraftRepository.findByPilotId(pilot.getId());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testOnePilotOneAircraft_Constraint() {
        // Arrange
        User user1 = new User();
        user1.setUsername("pilot1");
        user1.setPassword("password");
        user1.setRole(Role.PILOTE);
        user1 = entityManager.persistAndFlush(user1);

        Pilot pilot1 = new Pilot();
        pilot1.setName("Pilot 1");
        pilot1.setLicense("P1");
        pilot1.setUser(user1);
        pilot1 = entityManager.persistAndFlush(pilot1);

        Aircraft aircraft1 = new Aircraft();
        aircraft1.setRegistration("CN-A1");
        aircraft1.setModel("A320");
        aircraft1.setStatus(AircraftStatus.AU_SOL);
        aircraft1.setPilot(pilot1);
        aircraft1 = entityManager.persistAndFlush(aircraft1);

        User user2 = new User();
        user2.setUsername("pilot2");
        user2.setPassword("password");
        user2.setRole(Role.PILOTE);
        user2 = entityManager.persistAndFlush(user2);

        Pilot pilot2 = new Pilot();
        pilot2.setName("Pilot 2");
        pilot2.setLicense("P2");
        pilot2.setUser(user2);
        pilot2 = entityManager.persistAndFlush(pilot2);

        Aircraft aircraft2 = new Aircraft();
        aircraft2.setRegistration("CN-A2");
        aircraft2.setModel("A330");
        aircraft2.setStatus(AircraftStatus.AU_SOL);
        aircraft2.setPilot(pilot2);
        aircraft2 = entityManager.persistAndFlush(aircraft2);

        // Act
        Optional<Aircraft> result1 = aircraftRepository.findByPilotId(pilot1.getId());
        Optional<Aircraft> result2 = aircraftRepository.findByPilotId(pilot2.getId());

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(aircraft1.getId(), result1.get().getId());
        assertEquals(aircraft2.getId(), result2.get().getId());
        // Vérifier que chaque pilote a son propre avion
        assertNotEquals(result1.get().getId(), result2.get().getId());
    }
}

