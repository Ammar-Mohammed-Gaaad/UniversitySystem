package com._Person.university_system.repositories;

import com._Person.university_system.operations.Attendance;
import com._Person.university_system.AcademicStructure.Schedule;
import com._Person.university_system.users.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySchedule(Schedule schedule);
    List<Attendance> findByStudent(Student student);
    List<Attendance> findByScheduleAndDate(Schedule schedule, LocalDate date);
    Optional<Attendance> findByScheduleAndStudentAndDate(Schedule schedule, Student student, LocalDate date);
}
