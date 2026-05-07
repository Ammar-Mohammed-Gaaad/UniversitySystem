package com._Person.university_system.users;

import com._Person.university_system.AcademicStructure.Course;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student extends User {

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    private int enrollmentYear;

    private double gpa;

    @Column(nullable = false, unique = true)
    private String studentId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> enrolledCourses = new HashSet<>();

    public Student() {
    }

    public Student(String name, Long id, String password, String email, Role role,
                   String major, int enrollmentYear, double gpa, String studentId) {
        super(name, id, password, email, role);
        this.major = major;
        this.enrollmentYear = enrollmentYear;
        this.gpa = gpa;
        this.studentId = studentId;
    }

    public String getMajor() {
        return major;
    }

    public int getEnrollmentYear() {
        return enrollmentYear;
    }

    public double getGpa() {
        return gpa;
    }

    public String getStudentId() {
        return studentId;
    }

    public Set<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setEnrollmentYear(int enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setEnrolledCourses(Set<Course> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }
}
