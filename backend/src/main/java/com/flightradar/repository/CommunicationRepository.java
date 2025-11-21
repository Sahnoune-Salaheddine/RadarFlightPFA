package com.flightradar.repository;

import com.flightradar.model.Communication;
import com.flightradar.model.ReceiverType;
import com.flightradar.model.SenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, Long> {
    List<Communication> findBySenderTypeAndSenderId(SenderType senderType, Long senderId);
    List<Communication> findByReceiverTypeAndReceiverId(ReceiverType receiverType, Long receiverId);
    List<Communication> findBySenderTypeAndSenderIdOrReceiverTypeAndReceiverId(
        SenderType senderType, Long senderId, ReceiverType receiverType, Long receiverId
    );
    List<Communication> findAllByOrderByTimestampDesc();
}
