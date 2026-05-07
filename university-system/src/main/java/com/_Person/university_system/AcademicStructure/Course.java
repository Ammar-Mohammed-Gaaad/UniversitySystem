package com._Person.university_system.AcademicStructure;

import com._Person.university_system.users.Lecturer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String coursecode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String creditHours;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lecturer lecturer;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"courses", "hibernateLazyInitializer", "handler"})
    private Department department;

    public Course() {
    }

    public Course(String coursecode, String title, String creditHours, Lecturer lecturer) {
        this.coursecode = coursecode;
        this.title = title;
        this.creditHours = creditHours;
        this.lecturer = lecturer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public String getCoursecode() {
        return coursecode;
    }

    public String getTitle() {
        return title;
    }

    public String getCreditHours() {
        return creditHours;
    }

    public Department getDepartment() {
        return department;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public void setCoursecode(String coursecode) {
        this.coursecode = coursecode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreditHours(String creditHours) {
        this.creditHours = creditHours;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
