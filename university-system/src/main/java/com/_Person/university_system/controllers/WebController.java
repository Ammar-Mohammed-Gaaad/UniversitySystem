package com._Person.university_system.controllers;

import com._Person.university_system.AcademicStructure.*;
import com._Person.university_system.operations.*;
import com._Person.university_system.repositories.*;
import com._Person.university_system.users.*;
import com._Person.university_system.operations.Attendance.AttendanceStatus;
import com._Person.university_system.operations.Exam.ExamStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseMaterialRepository materialRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final PasswordEncoder passwordEncoder;

    public WebController(CourseRepository courseRepository, DepartmentRepository departmentRepository,
                         StudentRepository studentRepository, LecturerRepository lecturerRepository,
                         EnrollmentRepository enrollmentRepository, AssignmentRepository assignmentRepository,
                         AssignmentSubmissionRepository submissionRepository, CourseMaterialRepository materialRepository,
                         ScheduleRepository scheduleRepository, NotificationRepository notificationRepository,
                         AttendanceRepository attendanceRepository, ExamRepository examRepository,
                         QuestionRepository questionRepository, ExamSubmissionRepository examSubmissionRepository,
                         PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.materialRepository = materialRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
        this.attendanceRepository = attendanceRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examSubmissionRepository = examSubmissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // PUBLIC ROUTES
    // =========================================================

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/courses-page")
    public String coursesPage(Model model, Principal principal) {
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student ->
                model.addAttribute("enrolledIds",
                    student.getEnrolledCourses().stream().map(Course::getId).toList())
            );
        }
        return "courses";
    }

    @GetMapping("/departments-page")
    public String departmentsPage(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "departments";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/register/lecturer")
    public String registerLecturer() { return "register-lecturer"; }

    @GetMapping("/course-detail/{id}")
    public String courseDetail(@PathVariable Long id, Model model, Principal principal) {
        courseRepository.findById(id).ifPresentOrElse(course -> {
            model.addAttribute("course", course);
            long enrolledCount = studentRepository.findAll().stream()
                    .filter(s -> s.getEnrolledCourses().stream().anyMatch(c -> c.getId().equals(id)))
                    .count();
            model.addAttribute("enrolledCount", enrolledCount);
            model.addAttribute("materials", materialRepository.findByCourseIdOrderByUploadDateDesc(id));
            model.addAttribute("assignments", assignmentRepository.findByCourseId(id));
            List<Schedule> schedules = scheduleRepository.findByCourseId(id);
            model.addAttribute("schedules", schedules);

            if (principal != null) {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    boolean alreadyEnrolled = student.getEnrolledCourses().stream()
                            .anyMatch(c -> c.getId().equals(id));
                    model.addAttribute("alreadyEnrolled", alreadyEnrolled);
                });
                lecturerRepository.findByEmail(principal.getName()).ifPresent(lecturer -> {
                    model.addAttribute("isCourseLecturer",
                        course.getLecturer() != null && course.getLecturer().getID().equals(lecturer.getID()));
                });
            }
        }, () -> model.addAttribute("notFound", true));
        return "course-detail";
    }

    // =========================================================
    // ADMIN ROUTES
    // =========================================================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("lecturers", lecturerRepository.findAll());
        model.addAttribute("enrollments", enrollmentRepository.findAll());
        model.addAttribute("courseCount", courseRepository.count());
        model.addAttribute("departmentCount", departmentRepository.count());
        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("lecturerCount", lecturerRepository.count());
        model.addAttribute("enrollmentCount", enrollmentRepository.count());
        return "admin-dashboard";
    }

    @PostMapping("/admin/courses")
    public String addCourse(@RequestParam String coursecode, @RequestParam String title,
                            @RequestParam String creditHours, @RequestParam(required = false) Long departmentId,
                            @RequestParam(required = false) Long lecturerId,
                            RedirectAttributes ra) {
        Course course = new Course();
        course.setCoursecode(coursecode);
        course.setTitle(title);
        course.setCreditHours(creditHours);
        if (departmentId != null) departmentRepository.findById(departmentId).ifPresent(course::setDepartment);
        if (lecturerId != null) lecturerRepository.findById(lecturerId).ifPresent(course::setLecturer);
        courseRepository.save(course);
        ra.addFlashAttribute("successMessage", "Course added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/courses/edit/{id}")
    public String editCourse(@PathVariable Long id, @RequestParam String coursecode, @RequestParam String title,
                             @RequestParam String creditHours, @RequestParam(required = false) Long departmentId,
                             @RequestParam(required = false) Long lecturerId, RedirectAttributes ra) {
        courseRepository.findById(id).ifPresent(course -> {
            course.setCoursecode(coursecode);
            course.setTitle(title);
            course.setCreditHours(creditHours);
            course.setDepartment(departmentId != null ? departmentRepository.findById(departmentId).orElse(null) : null);
            course.setLecturer(lecturerId != null ? lecturerRepository.findById(lecturerId).orElse(null) : null);
            courseRepository.save(course);
            ra.addFlashAttribute("successMessage", "Course updated successfully!");
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Course deleted successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/departments")
    public String addDepartment(@RequestParam String name, @RequestParam(required = false) Long headId,
                                RedirectAttributes ra) {
        Department department = new Department();
        department.setName(name);
        if (headId != null) lecturerRepository.findById(headId).ifPresent(department::setHeadOfDepartment);
        departmentRepository.save(department);
        ra.addFlashAttribute("successMessage", "Department added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/departments/edit/{id}")
    public String editDepartment(@PathVariable Long id, @RequestParam String name,
                                 @RequestParam(required = false) Long headId, RedirectAttributes ra) {
        departmentRepository.findById(id).ifPresent(dept -> {
            dept.setName(name);
            dept.setHeadOfDepartment(headId != null ? lecturerRepository.findById(headId).orElse(null) : null);
            departmentRepository.save(dept);
            ra.addFlashAttribute("successMessage", "Department updated successfully!");
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes ra) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Department deleted successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/lecturers")
    public String addLecturer(@RequestParam String name, @RequestParam String email,
                              @RequestParam String password, @RequestParam(required = false) String department,
                              @RequestParam(required = false) String title, RedirectAttributes ra) {
        Lecturer lecturer = new Lecturer();
        lecturer.setName(name);
        lecturer.setEmail(email);
        lecturer.setPassword(passwordEncoder.encode(password));
        lecturer.setRole(User.Role.LECTURER);
        lecturer.setDepartment(department != null ? department : "General");
        lecturer.setTitle(title != null ? title : "Lecturer");
        lecturerRepository.save(lecturer);
        ra.addFlashAttribute("successMessage", "Lecturer added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/lecturers/delete/{id}")
    public String deleteLecturer(@PathVariable Long id, RedirectAttributes ra) {
        if (lecturerRepository.existsById(id)) {
            lecturerRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Lecturer removed successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    // =========================================================
    // STUDENT ROUTES
    // =========================================================

    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                model.addAttribute("enrolledCourses", student.getEnrolledCourses());
                model.addAttribute("student", student);
                int creditTotal = student.getEnrolledCourses().stream()
                        .mapToInt(c -> { try { return Integer.parseInt(c.getCreditHours()); } catch (NumberFormatException e) { return 0; } })
                        .sum();
                model.addAttribute("creditTotal", creditTotal);
                // Enrollment history with grades
                List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderBySemesterDesc(student.getID());
                model.addAttribute("enrollments", enrollments);
                // Calculate GPA
                double gpa = calculateGpa(enrollments);
                if (gpa >= 0) {
                    student.setGpa(gpa);
                    studentRepository.save(student);
                }
                // Upcoming assignments
                List<Long> courseIds = student.getEnrolledCourses().stream().map(Course::getId).toList();
                List<Assignment> upcoming = new ArrayList<>();
                for (Long cid : courseIds) {
                    upcoming.addAll(assignmentRepository.findByCourseId(cid));
                }
                upcoming.sort(Comparator.comparing(Assignment::getDueDate));
                model.addAttribute("upcomingAssignments", upcoming.stream().limit(5).toList());
            });
        }
        return "student-dashboard";
    }

    @GetMapping("/student/profile")
    public String studentProfile(Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                model.addAttribute("student", student);
                model.addAttribute("enrolledCourses", student.getEnrolledCourses());
                List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderBySemesterDesc(student.getID());
                model.addAttribute("enrollments", enrollments);
                int creditTotal = student.getEnrolledCourses().stream()
                        .mapToInt(c -> { try { return Integer.parseInt(c.getCreditHours()); } catch (NumberFormatException e) { return 0; } })
                        .sum();
                model.addAttribute("creditTotal", creditTotal);
            });
        }
        return "student-profile";
    }

    @GetMapping("/student/timetable")
    public String studentTimetable(Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                List<Long> courseIds = student.getEnrolledCourses().stream().map(Course::getId).toList();
                List<Schedule> schedules = courseIds.isEmpty() ? List.of() : scheduleRepository.findByCourseIdIn(courseIds);
                model.addAttribute("schedules", schedules);
                model.addAttribute("student", student);
            });
        }
        return "student-timetable";
    }

    @GetMapping("/student/course/{courseId}/assignments")
    public String studentAssignments(@PathVariable Long courseId, Model model, Principal principal) {
        courseRepository.findById(courseId).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
            model.addAttribute("assignments", assignments);
            if (principal != null) {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    Map<Long, AssignmentSubmission> submissionMap = new HashMap<>();
                    for (Assignment a : assignments) {
                        submissionRepository.findByStudentIdAndAssignmentId(student.getID(), a.getId())
                                .ifPresent(sub -> submissionMap.put(a.getId(), sub));
                    }
                    model.addAttribute("submissionMap", submissionMap);
                });
            }
        });
        return "student-assignments";
    }

    @PostMapping("/student/assignment/{assignmentId}/submit")
    public String submitAssignment(@PathVariable Long assignmentId, @RequestParam(required = false) String content,
                                   @RequestParam(required = false) MultipartFile file,
                                   Principal principal, RedirectAttributes ra) {
        assignmentRepository.findById(assignmentId).ifPresent(assignment -> {
            if (principal != null) {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    AssignmentSubmission sub = new AssignmentSubmission();
                    sub.setContent(content);
                    sub.setSubmissionDate(LocalDateTime.now());
                    sub.setStudent(student);
                    sub.setAssignment(assignment);
                    if (file != null && !file.isEmpty()) {
                        try {
                            String uploadDir = "uploads/submissions/";
                            Files.createDirectories(Paths.get(uploadDir));
                            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                            file.transferTo(new File(uploadDir + filename));
                            sub.setFileUrl("/uploads/submissions/" + filename);
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    submissionRepository.save(sub);
                    ra.addFlashAttribute("successMessage", "Assignment submitted!");
                });
            }
        });
        return "redirect:/student/course/" + assignmentRepository.findById(assignmentId)
                .map(a -> a.getCourse().getId()).orElse(0L) + "/assignments";
    }

    @PostMapping("/student/enroll/{courseId}")
    public String enrollCourse(@PathVariable Long courseId, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student ->
                courseRepository.findById(courseId).ifPresent(course -> {
                    boolean already = student.getEnrolledCourses().stream().anyMatch(c -> c.getId().equals(courseId));
                    if (already) {
                        ra.addFlashAttribute("errorMessage", "Already enrolled in " + course.getTitle() + ".");
                    } else {
                        student.getEnrolledCourses().add(course);
                        studentRepository.save(student);
                        // Create enrollment record
                        Enrollment enrollment = new Enrollment();
                        enrollment.setStudent(student);
                        enrollment.setCourse(course);
                        enrollment.setSemester(1);
                        enrollmentRepository.save(enrollment);
                        ra.addFlashAttribute("successMessage", "Enrolled in " + course.getTitle() + "!");
                    }
                })
            );
        }
        return "redirect:/courses-page";
    }

    @PostMapping("/student/unenroll/{courseId}")
    public String unenrollCourse(@PathVariable Long courseId, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student ->
                courseRepository.findById(courseId).ifPresent(course -> {
                    student.getEnrolledCourses().removeIf(c -> c.getId().equals(courseId));
                    studentRepository.save(student);
                    enrollmentRepository.findByStudentIdAndCourseId(student.getID(), courseId)
                            .ifPresent(enrollmentRepository::delete);
                    ra.addFlashAttribute("successMessage", "Dropped " + course.getTitle() + " successfully.");
                })
            );
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/course/{id}/attendance")
    public String studentAttendance(@PathVariable Long id, Model model, Principal principal) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            if (principal != null) {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    List<Schedule> schedules = scheduleRepository.findByCourseId(id);
                    List<Attendance> myAttendance = new ArrayList<>();
                    for (Schedule s : schedules) {
                        myAttendance.addAll(attendanceRepository.findBySchedule(s).stream()
                            .filter(a -> a.getStudent().getID().equals(student.getID()))
                            .toList());
                    }
                    myAttendance.sort((a,b) -> b.getDate().compareTo(a.getDate()));
                    model.addAttribute("myAttendance", myAttendance);
                    
                    long present = myAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
                    long absent = myAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
                    long excused = myAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
                    long total = present + absent + excused;
                    
                    model.addAttribute("presentCount", present);
                    model.addAttribute("absentCount", absent);
                    model.addAttribute("excusedCount", excused);
                    model.addAttribute("attendancePercentage", total > 0 ? (present * 100 / total) : 100);
                });
            }
        });
        return "student-attendance";
    }

    @GetMapping("/student/course/{id}/exams")
    public String studentExams(@PathVariable Long id, Model model, Principal principal) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Exam> exams = examRepository.findByCourseId(id);
            model.addAttribute("exams", exams);
            if (principal != null) {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    Map<Long, ExamSubmission> submissionMap = new HashMap<>();
                    for (Exam e : exams) {
                        examSubmissionRepository.findByExamIdAndStudentId(e.getId(), student.getID())
                                .ifPresent(sub -> submissionMap.put(e.getId(), sub));
                    }
                    model.addAttribute("submissionMap", submissionMap);
                });
            }
        });
        return "student-exams";
    }

    @GetMapping("/student/exam/{id}/take")
    public String takeExam(@PathVariable Long id, Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                examRepository.findById(id).ifPresent(exam -> {
                    if (exam.getStatus() == ExamStatus.PUBLISHED) {
                        model.addAttribute("exam", exam);
                        model.addAttribute("student", student);
                        boolean submitted = examSubmissionRepository.findByExamIdAndStudentId(id, student.getID()).isPresent();
                        model.addAttribute("alreadySubmitted", submitted);
                    }
                });
            });
        }
        return "student-take-exam";
    }

    @PostMapping("/student/exam/{id}/submit")
    public String submitExam(@PathVariable Long id, @RequestParam Map<String, String> answers, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                examRepository.findById(id).ifPresent(exam -> {
                    if (examSubmissionRepository.findByExamIdAndStudentId(id, student.getID()).isEmpty()) {
                        int score = 0;
                        int total = 0;
                        for (Question q : exam.getQuestions()) {
                            total += q.getPoints();
                            String studentAnswer = answers.get("question_" + q.getId());
                            if (studentAnswer != null && studentAnswer.equals(q.getCorrectAnswer())) {
                                score += q.getPoints();
                            }
                        }
                        ExamSubmission sub = new ExamSubmission();
                        sub.setExam(exam);
                        sub.setStudent(student);
                        sub.setSubmissionTime(LocalDateTime.now());
                        sub.setScore(score);
                        sub.setTotalPossibleScore(total);
                        examSubmissionRepository.save(sub);
                        ra.addFlashAttribute("successMessage", "Exam submitted successfully! Score: " + score + "/" + total);
                    } else {
                        ra.addFlashAttribute("errorMessage", "You have already submitted this exam.");
                    }
                });
            });
        }
        Long courseId = examRepository.findById(id).map(e -> e.getCourse().getId()).orElse(0L);
        return "redirect:/student/course/" + courseId + "/exams";
    }

    // =========================================================
    // LECTURER ROUTES
    // =========================================================

    @GetMapping("/lecturer/dashboard")
    public String lecturerDashboard(Model model, Principal principal) {
        if (principal != null) {
            lecturerRepository.findByEmail(principal.getName()).ifPresent(lecturer -> {
                model.addAttribute("lecturer", lecturer);
                List<Course> courses = courseRepository.findAll().stream()
                        .filter(c -> c.getLecturer() != null && c.getLecturer().getID().equals(lecturer.getID()))
                        .toList();
                model.addAttribute("courses", courses);
                long totalStudents = courses.stream()
                        .flatMap(c -> studentRepository.findAll().stream()
                                .filter(s -> s.getEnrolledCourses().stream().anyMatch(ec -> ec.getId().equals(c.getId()))))
                        .distinct().count();
                model.addAttribute("totalStudents", totalStudents);
                // Pending submissions count
                long pendingSubs = 0;
                for (Course c : courses) {
                    for (Assignment a : assignmentRepository.findByCourseId(c.getId())) {
                        pendingSubs += submissionRepository.findByAssignmentId(a.getId()).stream()
                                .filter(s -> s.getGrade() == null).count();
                    }
                }
                model.addAttribute("pendingSubmissions", pendingSubs);
            });
        }
        return "lecturer-dashboard";
    }

    @GetMapping("/lecturer/course/{id}/grades")
    public String lecturerGrades(@PathVariable Long id, Model model, Principal principal) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(id);
            model.addAttribute("enrollments", enrollments);
        });
        return "lecturer-grades";
    }

    @PostMapping("/lecturer/course/{id}/grades")
    public String saveGrades(@PathVariable Long id, @RequestParam Map<String, String> grades, RedirectAttributes ra) {
        grades.forEach((key, value) -> {
            if (key.startsWith("grade_")) {
                Long enrollmentId = Long.parseLong(key.substring(6));
                enrollmentRepository.findById(enrollmentId).ifPresent(e -> {
                    e.setGrade(value);
                    enrollmentRepository.save(e);
                });
            }
        });
        ra.addFlashAttribute("successMessage", "Grades saved successfully!");
        return "redirect:/lecturer/course/" + id + "/grades";
    }

    @GetMapping("/lecturer/course/{id}/assignments")
    public String lecturerAssignments(@PathVariable Long id, Model model) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Assignment> assignments = assignmentRepository.findByCourseId(id);
            model.addAttribute("assignments", assignments);
            Map<Long, Long> subCounts = new HashMap<>();
            for (Assignment a : assignments) {
                subCounts.put(a.getId(), (long) submissionRepository.findByAssignmentId(a.getId()).size());
            }
            model.addAttribute("subCounts", subCounts);
        });
        return "lecturer-assignments";
    }

    @PostMapping("/lecturer/course/{id}/assignments")
    public String createAssignment(@PathVariable Long id, @RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam String dueDate, @RequestParam int maxScore, RedirectAttributes ra) {
        courseRepository.findById(id).ifPresent(course -> {
            Assignment a = new Assignment();
            a.setTitle(title);
            a.setDescription(description);
            a.setDueDate(LocalDateTime.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            a.setMaxScore(maxScore);
            a.setCourse(course);
            assignmentRepository.save(a);
            ra.addFlashAttribute("successMessage", "Assignment created!");
        });
        return "redirect:/lecturer/course/" + id + "/assignments";
    }

    @GetMapping("/lecturer/assignment/{id}/submissions")
    public String lecturerSubmissions(@PathVariable Long id, Model model) {
        assignmentRepository.findById(id).ifPresent(assignment -> {
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", submissionRepository.findByAssignmentId(id));
        });
        return "lecturer-submissions";
    }

    @PostMapping("/lecturer/submission/{id}/grade")
    public String gradeSubmission(@PathVariable Long id, @RequestParam Double grade,
                                  @RequestParam(required = false) String feedback, RedirectAttributes ra) {
        submissionRepository.findById(id).ifPresent(sub -> {
            sub.setGrade(grade);
            sub.setFeedback(feedback);
            submissionRepository.save(sub);
            ra.addFlashAttribute("successMessage", "Submission graded!");
        });
        Long assignmentId = submissionRepository.findById(id).map(s -> s.getAssignment().getId()).orElse(0L);
        return "redirect:/lecturer/assignment/" + assignmentId + "/submissions";
    }

    @GetMapping("/lecturer/course/{id}/materials")
    public String lecturerMaterials(@PathVariable Long id, Model model) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("materials", materialRepository.findByCourseIdOrderByUploadDateDesc(id));
        });
        return "lecturer-materials";
    }

    @PostMapping("/lecturer/course/{id}/materials")
    public String uploadMaterial(@PathVariable Long id, @RequestParam String title,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) MultipartFile file, RedirectAttributes ra) {
        courseRepository.findById(id).ifPresent(course -> {
            CourseMaterial mat = new CourseMaterial();
            mat.setTitle(title);
            mat.setDescription(description);
            mat.setUploadDate(LocalDateTime.now());
            mat.setCourse(course);
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = "uploads/materials/";
                    Files.createDirectories(Paths.get(uploadDir));
                    String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(uploadDir + filename));
                    mat.setFileUrl("/uploads/materials/" + filename);
                } catch (IOException e) { e.printStackTrace(); }
            }
            materialRepository.save(mat);
            ra.addFlashAttribute("successMessage", "Material uploaded!");
        });
        return "redirect:/lecturer/course/" + id + "/materials";
    }

    @GetMapping("/lecturer/timetable")
    public String lecturerTimetable(Model model, Principal principal) {
        if (principal != null) {
            lecturerRepository.findByEmail(principal.getName()).ifPresent(lecturer -> {
                model.addAttribute("lecturer", lecturer);
                model.addAttribute("schedules", scheduleRepository.findByLecturerId(lecturer.getID()));
            });
        }
        return "lecturer-timetable";
    }

    @GetMapping("/lecturer/course/{id}/attendance")
    public String lecturerAttendance(@PathVariable Long id, @RequestParam(required = false) Long scheduleId, @RequestParam(required = false) String date, Model model) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Schedule> schedules = scheduleRepository.findByCourseId(id);
            model.addAttribute("schedules", schedules);
            
            if (scheduleId != null) {
                scheduleRepository.findById(scheduleId).ifPresent(schedule -> {
                    model.addAttribute("selectedSchedule", schedule);
                    LocalDate parsedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
                    model.addAttribute("selectedDate", parsedDate);
                    
                    List<Enrollment> enrollments = enrollmentRepository.findByCourseId(id);
                    List<Student> students = enrollments.stream().map(Enrollment::getStudent).toList();
                    model.addAttribute("students", students);
                    
                    List<Attendance> existingAttendance = attendanceRepository.findByScheduleAndDate(schedule, parsedDate);
                    Map<Long, String> attendanceMap = new HashMap<>();
                    for (Attendance a : existingAttendance) {
                        attendanceMap.put(a.getStudent().getID(), a.getStatus().name());
                    }
                    model.addAttribute("attendanceMap", attendanceMap);
                });
            }
        });
        return "lecturer-attendance";
    }

    @PostMapping("/lecturer/course/{id}/attendance/save")
    public String saveAttendance(@PathVariable Long id, @RequestParam Long scheduleId, @RequestParam String date, @RequestParam Map<String, String> params, RedirectAttributes ra) {
        scheduleRepository.findById(scheduleId).ifPresent(schedule -> {
            LocalDate parsedDate = LocalDate.parse(date);
            params.forEach((key, value) -> {
                if (key.startsWith("status_")) {
                    Long studentId = Long.parseLong(key.substring(7));
                    studentRepository.findById(studentId).ifPresent(student -> {
                        Attendance attendance = attendanceRepository.findByScheduleAndStudentAndDate(schedule, student, parsedDate)
                                .orElse(new Attendance(schedule, student, parsedDate, AttendanceStatus.PRESENT));
                        attendance.setStatus(AttendanceStatus.valueOf(value));
                        attendanceRepository.save(attendance);
                    });
                }
            });
            ra.addFlashAttribute("successMessage", "Attendance saved successfully!");
        });
        return "redirect:/lecturer/course/" + id + "/attendance?scheduleId=" + scheduleId + "&date=" + date;
    }

    @GetMapping("/lecturer/course/{id}/exams")
    public String lecturerExams(@PathVariable Long id, Model model) {
        courseRepository.findById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            List<Exam> exams = examRepository.findByCourseId(id);
            model.addAttribute("exams", exams);
            Map<Long, Long> subCounts = new HashMap<>();
            for (Exam e : exams) {
                subCounts.put(e.getId(), (long) examSubmissionRepository.findByExamId(e.getId()).size());
            }
            model.addAttribute("subCounts", subCounts);
        });
        return "lecturer-exams";
    }

    @PostMapping("/lecturer/course/{id}/exams")
    public String createExam(@PathVariable Long id, @RequestParam String title, @RequestParam(required = false) String description, @RequestParam String startTime, @RequestParam int durationMinutes, RedirectAttributes ra) {
        courseRepository.findById(id).ifPresent(course -> {
            Exam exam = new Exam();
            exam.setTitle(title);
            exam.setDescription(description);
            exam.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            exam.setDurationMinutes(durationMinutes);
            exam.setCourse(course);
            exam.setStatus(ExamStatus.DRAFT);
            examRepository.save(exam);
            ra.addFlashAttribute("successMessage", "Exam created!");
        });
        return "redirect:/lecturer/course/" + id + "/exams";
    }

    @GetMapping("/lecturer/exam/{id}")
    public String manageExam(@PathVariable Long id, Model model) {
        examRepository.findById(id).ifPresent(exam -> {
            model.addAttribute("exam", exam);
            model.addAttribute("course", exam.getCourse());
        });
        return "lecturer-exam-detail";
    }

    @PostMapping("/lecturer/exam/{id}/questions")
    public String addQuestion(@PathVariable Long id, @RequestParam String text, @RequestParam String options, @RequestParam String correctAnswer, @RequestParam int points, RedirectAttributes ra) {
        examRepository.findById(id).ifPresent(exam -> {
            Question q = new Question();
            q.setText(text);
            q.setOptions(options);
            q.setCorrectAnswer(correctAnswer);
            q.setPoints(points);
            q.setExam(exam);
            questionRepository.save(q);
            ra.addFlashAttribute("successMessage", "Question added!");
        });
        return "redirect:/lecturer/exam/" + id;
    }

    @PostMapping("/lecturer/exam/{id}/publish")
    public String publishExam(@PathVariable Long id, RedirectAttributes ra) {
        examRepository.findById(id).ifPresent(exam -> {
            exam.setStatus(ExamStatus.PUBLISHED);
            examRepository.save(exam);
            ra.addFlashAttribute("successMessage", "Exam published!");
        });
        return "redirect:/lecturer/course/" + examRepository.findById(id).map(e -> e.getCourse().getId()).orElse(0L) + "/exams";
    }

    @GetMapping("/lecturer/exam/{id}/submissions")
    public String examSubmissions(@PathVariable Long id, Model model) {
        examRepository.findById(id).ifPresent(exam -> {
            model.addAttribute("exam", exam);
            model.addAttribute("course", exam.getCourse());
            model.addAttribute("submissions", examSubmissionRepository.findByExamId(id));
        });
        return "lecturer-exam-submissions";
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(s ->
                model.addAttribute("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(s.getID())));
            lecturerRepository.findByEmail(principal.getName()).ifPresent(l ->
                model.addAttribute("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(l.getID())));
        }
        return "notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable Long id, RedirectAttributes ra) {
        notificationRepository.findById(id).ifPresent(n -> { n.setRead(true); notificationRepository.save(n); });
        return "redirect:/notifications";
    }

    // =========================================================
    // GPA CALCULATION HELPER
    // =========================================================

    private double calculateGpa(List<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) return -1;
        List<Enrollment> graded = enrollments.stream().filter(e -> e.getGrade() != null && !e.getGrade().isBlank()).toList();
        if (graded.isEmpty()) return -1;
        double totalPoints = 0; int totalCredits = 0;
        for (Enrollment e : graded) {
            int credits;
            try { credits = Integer.parseInt(e.getCourse().getCreditHours()); } catch (Exception ex) { credits = 3; }
            double points = gradeToPoints(e.getGrade());
            totalPoints += points * credits;
            totalCredits += credits;
        }
        return totalCredits > 0 ? Math.round((totalPoints / totalCredits) * 100.0) / 100.0 : -1;
    }

    private double gradeToPoints(String grade) {
        return switch (grade.toUpperCase().trim()) {
            case "A+", "A" -> 4.0; case "A-" -> 3.7; case "B+" -> 3.3; case "B" -> 3.0; case "B-" -> 2.7;
            case "C+" -> 2.3; case "C" -> 2.0; case "C-" -> 1.7; case "D+" -> 1.3; case "D" -> 1.0; case "F" -> 0.0;
            default -> 0.0;
        };
    }
}
