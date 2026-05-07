package com._Person.university_system.repositories;

import com._Person.university_system.AcademicStructure.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDayOfWeek(String dayOfWeek);
    List<Schedule> findByLecturerId(Long lecturerId);
    List<Schedule> findByCourseId(Long courseId);
    List<Schedule> findByCourseIdIn(List<Long> courseIds);
}
