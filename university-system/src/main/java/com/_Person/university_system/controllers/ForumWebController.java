package com._Person.university_system.controllers;

import com._Person.university_system.operations.ForumThread;
import com._Person.university_system.operations.ForumReply;
import com._Person.university_system.repositories.ForumThreadRepository;
import com._Person.university_system.repositories.ForumReplyRepository;
import com._Person.university_system.repositories.CourseRepository;
import com._Person.university_system.repositories.StudentRepository;
import com._Person.university_system.repositories.LecturerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ForumWebController {

    private final ForumThreadRepository threadRepository;
    private final ForumReplyRepository replyRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public ForumWebController(ForumThreadRepository threadRepository, ForumReplyRepository replyRepository,
                              CourseRepository courseRepository, StudentRepository studentRepository,
                              LecturerRepository lecturerRepository) {
        this.threadRepository = threadRepository;
        this.replyRepository = replyRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    private String resolveUserName(String email) {
        return studentRepository.findByEmail(email).map(s -> s.getName())
                .orElseGet(() -> lecturerRepository.findByEmail(email).map(l -> l.getName()).orElse(email));
    }

    @GetMapping("/course/{courseId}/forum")
    public String courseForum(@PathVariable Long courseId, Model model, Principal principal) {
        courseRepository.findById(courseId).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("threads", threadRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
        });
        return "course-forum";
    }

    @PostMapping("/course/{courseId}/forum/thread")
    public String createThread(@PathVariable Long courseId, @RequestParam String title, @RequestParam String content, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            courseRepository.findById(courseId).ifPresent(course -> {
                ForumThread thread = new ForumThread();
                thread.setCourse(course);
                thread.setTitle(title);
                thread.setContent(content);
                thread.setAuthorEmail(principal.getName());
                thread.setAuthorName(resolveUserName(principal.getName()));
                thread.setCreatedAt(LocalDateTime.now());
                threadRepository.save(thread);
                ra.addFlashAttribute("successMessage", "Discussion created!");
            });
        }
        return "redirect:/course/" + courseId + "/forum";
    }

    @GetMapping("/forum/thread/{threadId}")
    public String viewThread(@PathVariable Long threadId, Model model) {
        threadRepository.findById(threadId).ifPresent(thread -> {
            model.addAttribute("thread", thread);
            model.addAttribute("course", thread.getCourse());
        });
        return "forum-thread";
    }

    @PostMapping("/forum/thread/{threadId}/reply")
    public String replyToThread(@PathVariable Long threadId, @RequestParam String content, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            threadRepository.findById(threadId).ifPresent(thread -> {
                ForumReply reply = new ForumReply();
                reply.setThread(thread);
                reply.setContent(content);
                reply.setAuthorEmail(principal.getName());
                reply.setAuthorName(resolveUserName(principal.getName()));
                reply.setCreatedAt(LocalDateTime.now());
                replyRepository.save(reply);
                ra.addFlashAttribute("successMessage", "Reply posted!");
            });
        }
        return "redirect:/forum/thread/" + threadId;
    }
}
