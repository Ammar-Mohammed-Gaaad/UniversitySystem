package com._Person.university_system.repositories;

import com._Person.university_system.operations.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumReplyRepository extends JpaRepository<ForumReply, Long> {
}
