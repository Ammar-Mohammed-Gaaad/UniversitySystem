package com._Person.university_system.operations;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // For multiple choice questions, store options as JSON or comma-separated
    // We'll use a simple delimiter string like "Option A||Option B||Option C"
    @Column(columnDefinition = "TEXT")
    private String options;

    @Column(nullable = false)
    private String correctAnswer;

    private int points = 1;

    public Question() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public enum QuestionType { MCQ, TRUE_FALSE, SHORT_ANSWER }

    @Enumerated(EnumType.STRING)
    private QuestionType questionType = QuestionType.MCQ;
    private double negativeMark = 0.0;

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public double getNegativeMark() { return negativeMark; }
    public void setNegativeMark(double negativeMark) { this.negativeMark = negativeMark; }
}
