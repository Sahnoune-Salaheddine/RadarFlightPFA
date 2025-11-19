package com.flightradar.repository;

import com.flightradar.model.ATCMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ATCMessageRepository extends JpaRepository<ATCMessage, Long> {
    List<ATCMessage> findByAircraftIdOrderByTimestampDesc(Long aircraftId);
    List<ATCMessage> findByRadarIdOrderByTimestampDesc(Long radarId);
    List<ATCMessage> findByPilotIdOrderByTimestampDesc(Long pilotId);
    List<ATCMessage> findByAircraftIdAndTypeOrderByTimestampDesc(Long aircraftId, ATCMessage.ATCMessageType type);
}

