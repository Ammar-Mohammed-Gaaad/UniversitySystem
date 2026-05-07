package com._Person.university_system.repositories;

import com._Person.university_system.operations.Exam;
import com._Person.university_system.AcademicStructure.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseId(Long courseId);
    List<Exam> findByCourse(Course course);
}
