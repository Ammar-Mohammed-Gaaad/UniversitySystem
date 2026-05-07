package com._Person.university_system.AcademicStructure;

import com._Person.university_system.users.Lecturer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne
    @JoinColumn(name = "head_of_department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lecturer headOfDepartment;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"department", "hibernateLazyInitializer", "handler"})
    private List<Course> courses;

    public Department() {
    }

    public Department(Long id, String name, Lecturer headOfDepartment, List<Course> courses) {
        this.id = id;
        this.name = name;
        this.headOfDepartment = headOfDepartment;
        this.courses = courses;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Lecturer getHeadOfDepartment() {
        return headOfDepartment;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHeadOfDepartment(Lecturer headOfDepartment) {
        this.headOfDepartment = headOfDepartment;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
