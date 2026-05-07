package com._Person.university_system.controllers;

import com._Person.university_system.users.Lecturer;
import com._Person.university_system.repositories.LecturerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerController {

    private final LecturerRepository lecturerRepository;

    public LecturerController(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    @GetMapping
    public List<Lecturer> getAll() {
        return lecturerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lecturer> getById(@PathVariable Long id) {
        return lecturerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public long count() {
        return lecturerRepository.count();
    }

    @PostMapping
    public Lecturer create(@RequestBody Lecturer lecturer) {
        lecturer.setRole(com._Person.university_system.users.User.Role.LECTURER);
        return lecturerRepository.save(lecturer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lecturer> update(@PathVariable Long id, @RequestBody Lecturer updated) {
        return lecturerRepository.findById(id).map(l -> {
            l.setName(updated.getName());
            l.setEmail(updated.getEmail());
            l.setTitle(updated.getTitle());
            l.setDepartment(updated.getDepartment());
            l.setOfficeHours(updated.getOfficeHours());
            return ResponseEntity.ok(lecturerRepository.save(l));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (lecturerRepository.existsById(id)) {
            lecturerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
