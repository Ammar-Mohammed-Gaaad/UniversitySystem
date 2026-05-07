package com._Person.university_system.controllers;

import com._Person.university_system.repositories.LecturerRepository;
import com._Person.university_system.repositories.StudentRepository;
import com._Person.university_system.security.JwtUtil;
import com._Person.university_system.users.Lecturer;
import com._Person.university_system.users.Student;
import com._Person.university_system.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepo;
    private final LecturerRepository lecturerRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(StudentRepository studentRepo, LecturerRepository lecturerRepo,
                          JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.lecturerRepo = lecturerRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // Check students first
        var studentOpt = studentRepo.findByEmail(email);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            if (passwordEncoder.matches(password, s.getPassword())) {
                String token = jwtUtil.generateToken(email, "STUDENT", s.getID());
                return ResponseEntity.ok(buildResponse(token, s.getName(), "STUDENT", s.getID()));
            }
        }

        // Check lecturers
        var lecturerOpt = lecturerRepo.findByEmail(email);
        if (lecturerOpt.isPresent()) {
            Lecturer l = lecturerOpt.get();
            if (passwordEncoder.matches(password, l.getPassword())) {
                String token = jwtUtil.generateToken(email, "LECTURER", l.getID());
                return ResponseEntity.ok(buildResponse(token, l.getName(), "LECTURER", l.getID()));
            }
        }

        // Check for admin (simple hardcoded for now)
        if ("admin@creativa.edu".equals(email) && "admin123".equals(password)) {
            String token = jwtUtil.generateToken(email, "ADMIN", 0L);
            return ResponseEntity.ok(buildResponse(token, "Admin", "ADMIN", 0L));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, String> request) {
        if (studentRepo.findByEmail(request.get("email")).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }
        Student student = new Student();
        student.setName(request.get("name"));
        student.setEmail(request.get("email"));
        student.setPassword(passwordEncoder.encode(request.get("password")));
        student.setRole(User.Role.STUDENT);
        student.setMajor(request.getOrDefault("major", "Undeclared"));
        student.setEnrollmentYear(Integer.parseInt(request.getOrDefault("enrollmentYear", "2026")));
        student.setStudentId(request.getOrDefault("studentId", "STU-" + System.currentTimeMillis()));
        student.setGpa(0.0);

        Student saved = studentRepo.save(student);
        String token = jwtUtil.generateToken(saved.getEmail(), "STUDENT", saved.getID());
        return ResponseEntity.ok(buildResponse(token, saved.getName(), "STUDENT", saved.getID()));
    }

    @PostMapping("/register/lecturer")
    public ResponseEntity<?> registerLecturer(@RequestBody Map<String, String> request) {
        if (lecturerRepo.findByEmail(request.get("email")).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }
        Lecturer lecturer = new Lecturer();
        lecturer.setName(request.get("name"));
        lecturer.setEmail(request.get("email"));
        lecturer.setPassword(passwordEncoder.encode(request.get("password")));
        lecturer.setRole(User.Role.LECTURER);
        lecturer.setDepartment(request.getOrDefault("department", "General"));
        lecturer.setTitle(request.getOrDefault("title", "Lecturer"));
        lecturer.setOfficeHours(request.getOrDefault("officeHours", ""));

        Lecturer saved = lecturerRepo.save(lecturer);
        String token = jwtUtil.generateToken(saved.getEmail(), "LECTURER", saved.getID());
        return ResponseEntity.ok(buildResponse(token, saved.getName(), "LECTURER", saved.getID()));
    }

    private Map<String, Object> buildResponse(String token, String name, String role, Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("name", name);
        response.put("role", role);
        response.put("userId", userId);
        return response;
    }
}
