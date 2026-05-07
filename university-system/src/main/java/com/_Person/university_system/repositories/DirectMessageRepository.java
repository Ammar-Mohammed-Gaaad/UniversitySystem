package com._Person.university_system.repositories;

import com._Person.university_system.operations.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    List<DirectMessage> findBySenderEmailOrReceiverEmailOrderBySentAtDesc(String senderEmail, String receiverEmail);
    List<DirectMessage> findByReceiverEmailAndIsReadFalse(String receiverEmail);
}
