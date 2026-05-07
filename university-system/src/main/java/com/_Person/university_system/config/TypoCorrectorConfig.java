package com._Person.university_system.config;

import com._Person.university_system.AcademicStructure.Course;
import com._Person.university_system.AcademicStructure.Department;
import com._Person.university_system.repositories.CourseRepository;
import com._Person.university_system.repositories.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TypoCorrectorConfig {

    @Bean
    CommandLineRunner correctTypos(DepartmentRepository departmentRepository, CourseRepository courseRepository) {
        return args -> {
            List<Department> departments = departmentRepository.findAll();
            for (Department dept : departments) {
                if ("Phrmasy".equalsIgnoreCase(dept.getName())) {
                    dept.setName("Pharmacy");
                    departmentRepository.save(dept);
                }
            }

            List<Course> courses = courseRepository.findAll();
            for (Course course : courses) {
                if ("Advanced Programing".equalsIgnoreCase(course.getTitle())) {
                    course.setTitle("Advanced Programming");
                    courseRepository.save(course);
                }
            }
        };
    }
}
