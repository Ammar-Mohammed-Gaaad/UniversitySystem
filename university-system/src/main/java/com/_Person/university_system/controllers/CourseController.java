package com._Person.university_system.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com._Person.university_system.AcademicStructure.Course;
import com._Person.university_system.repositories.CourseRepository;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public long count() {
        return courseRepository.count();
    }

    @PostMapping
    public ResponseEntity<?> addCourse(@RequestBody java.util.Map<String, Object> payload) {
        Course course = new Course();
        course.setCoursecode((String) payload.get("coursecode"));
        course.setTitle((String) payload.get("title"));
        course.setCreditHours(String.valueOf(payload.get("creditHours")));

        if (payload.get("lecturer") instanceof java.util.Map) {
            java.util.Map<?, ?> lMap = (java.util.Map<?, ?>) payload.get("lecturer");
            if (lMap.get("id") != null) {
                com._Person.university_system.users.Lecturer l = new com._Person.university_system.users.Lecturer();
                l.setID(Long.valueOf(lMap.get("id").toString()));
                course.setLecturer(l);
            }
        }
        if (payload.get("department") instanceof java.util.Map) {
            java.util.Map<?, ?> dMap = (java.util.Map<?, ?>) payload.get("department");
            if (dMap.get("id") != null) {
                com._Person.university_system.AcademicStructure.Department d = new com._Person.university_system.AcademicStructure.Department();
                d.setId(Long.valueOf(dMap.get("id").toString()));
                course.setDepartment(d);
            }
        }
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {
        return courseRepository.findById(id).map(c -> {
            c.setCoursecode((String) payload.get("coursecode"));
            c.setTitle((String) payload.get("title"));
            c.setCreditHours(String.valueOf(payload.get("creditHours")));

            if (payload.get("lecturer") instanceof java.util.Map) {
                java.util.Map<?, ?> lMap = (java.util.Map<?, ?>) payload.get("lecturer");
                if (lMap.get("id") != null) {
                    com._Person.university_system.users.Lecturer l = new com._Person.university_system.users.Lecturer();
                    l.setID(Long.valueOf(lMap.get("id").toString()));
                    c.setLecturer(l);
                } else c.setLecturer(null);
            } else c.setLecturer(null);

            if (payload.get("department") instanceof java.util.Map) {
                java.util.Map<?, ?> dMap = (java.util.Map<?, ?>) payload.get("department");
                if (dMap.get("id") != null) {
                    com._Person.university_system.AcademicStructure.Department d = new com._Person.university_system.AcademicStructure.Department();
                    d.setId(Long.valueOf(dMap.get("id").toString()));
                    c.setDepartment(d);
                } else c.setDepartment(null);
            } else c.setDepartment(null);

            return ResponseEntity.ok(courseRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}