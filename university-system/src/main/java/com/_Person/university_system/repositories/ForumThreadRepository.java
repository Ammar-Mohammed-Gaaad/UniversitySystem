package com._Person.university_system.repositories;

import com._Person.university_system.operations.ForumThread;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    List<ForumThread> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
