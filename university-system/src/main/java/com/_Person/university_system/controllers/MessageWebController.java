package com._Person.university_system.controllers;

import com._Person.university_system.operations.DirectMessage;
import com._Person.university_system.repositories.DirectMessageRepository;
import com._Person.university_system.repositories.StudentRepository;
import com._Person.university_system.repositories.LecturerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class MessageWebController {

    private final DirectMessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public MessageWebController(DirectMessageRepository messageRepository, StudentRepository studentRepository, LecturerRepository lecturerRepository) {
        this.messageRepository = messageRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    private String resolveUserName(String email) {
        return studentRepository.findByEmail(email).map(s -> s.getName())
                .orElseGet(() -> lecturerRepository.findByEmail(email).map(l -> l.getName()).orElse(email));
    }

    @GetMapping("/messages")
    public String inbox(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            List<DirectMessage> messages = messageRepository.findBySenderEmailOrReceiverEmailOrderBySentAtDesc(email, email);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUserEmail", email);
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("lecturers", lecturerRepository.findAll());
        }
        return "messages";
    }

    @PostMapping("/messages/send")
    public String sendMessage(@RequestParam String receiverEmail, @RequestParam String content, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            DirectMessage msg = new DirectMessage();
            msg.setSenderEmail(principal.getName());
            msg.setSenderName(resolveUserName(principal.getName()));
            msg.setReceiverEmail(receiverEmail);
            msg.setReceiverName(resolveUserName(receiverEmail));
            msg.setContent(content);
            msg.setSentAt(LocalDateTime.now());
            messageRepository.save(msg);
            ra.addFlashAttribute("successMessage", "Message sent!");
        }
        return "redirect:/messages";
    }
}
