package com._Person.university_system.repositories;

import com._Person.university_system.operations.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    List<ExamSubmission> findByExamId(Long examId);
    List<ExamSubmission> findByStudentId(Long studentId);
    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);
}
