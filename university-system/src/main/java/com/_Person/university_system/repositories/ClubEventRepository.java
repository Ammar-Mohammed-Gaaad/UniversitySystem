package com._Person.university_system.repositories;

import com._Person.university_system.operations.ClubEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubEventRepository extends JpaRepository<ClubEvent, Long> {
    List<ClubEvent> findByClubId(Long clubId);
}
