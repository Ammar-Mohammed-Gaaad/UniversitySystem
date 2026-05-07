package com._Person.university_system.operations;

import com._Person.university_system.users.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_submissions")
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDateTime submissionTime;

    private int score;
    private int totalPossibleScore;

    // Store student's answers as JSON or simple delimited string
    @Column(columnDefinition = "TEXT")
    private String studentAnswers;

    public ExamSubmission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(LocalDateTime submissionTime) { this.submissionTime = submissionTime; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalPossibleScore() { return totalPossibleScore; }
    public void setTotalPossibleScore(int totalPossibleScore) { this.totalPossibleScore = totalPossibleScore; }
    public String getStudentAnswers() { return studentAnswers; }
    public void setStudentAnswers(String studentAnswers) { this.studentAnswers = studentAnswers; }
}
