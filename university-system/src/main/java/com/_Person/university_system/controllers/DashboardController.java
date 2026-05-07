package com._Person.university_system.controllers;

import com._Person.university_system.repositories.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentRepository studentRepo;
    private final LecturerRepository lecturerRepo;
    private final CourseRepository courseRepo;
    private final DepartmentRepository departmentRepo;
    private final EnrollmentRepository enrollmentRepo;

    public DashboardController(StudentRepository studentRepo, LecturerRepository lecturerRepo,
                               CourseRepository courseRepo, DepartmentRepository departmentRepo,
                               EnrollmentRepository enrollmentRepo) {
        this.studentRepo = studentRepo;
        this.lecturerRepo = lecturerRepo;
        this.courseRepo = courseRepo;
        this.departmentRepo = departmentRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("students", studentRepo.count());
        stats.put("lecturers", lecturerRepo.count());
        stats.put("courses", courseRepo.count());
        stats.put("departments", departmentRepo.count());
        stats.put("enrollments", enrollmentRepo.count());
        return stats;
    }
}
