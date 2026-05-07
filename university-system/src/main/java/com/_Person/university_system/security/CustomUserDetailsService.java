package com._Person.university_system.security;

import com._Person.university_system.repositories.LecturerRepository;
import com._Person.university_system.repositories.StudentRepository;
import com._Person.university_system.users.Lecturer;
import com._Person.university_system.users.Student;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepo;
    private final LecturerRepository lecturerRepo;

    public CustomUserDetailsService(StudentRepository studentRepo, LecturerRepository lecturerRepo) {
        this.studentRepo = studentRepo;
        this.lecturerRepo = lecturerRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Hardcoded admin check
        if ("admin@creativa.edu".equals(email)) {
            return User.builder()
                    .username("admin@creativa.edu")
                    .password("{noop}admin123")
                    .roles("ADMIN")
                    .build();
        }

        Optional<Student> studentOpt = studentRepo.findByEmail(email);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            return User.builder()
                    .username(s.getEmail())
                    .password(s.getPassword())
                    .roles("STUDENT")
                    .build();
        }

        Optional<Lecturer> lecturerOpt = lecturerRepo.findByEmail(email);
        if (lecturerOpt.isPresent()) {
            Lecturer l = lecturerOpt.get();
            return User.builder()
                    .username(l.getEmail())
                    .password(l.getPassword())
                    .roles("LECTURER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
