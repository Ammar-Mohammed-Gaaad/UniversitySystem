package com._Person.university_system.controllers;

import com._Person.university_system.operations.Club;
import com._Person.university_system.operations.ClubEvent;
import com._Person.university_system.repositories.ClubRepository;
import com._Person.university_system.repositories.ClubEventRepository;
import com._Person.university_system.repositories.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class ClubWebController {

    private final ClubRepository clubRepository;
    private final ClubEventRepository eventRepository;
    private final StudentRepository studentRepository;

    public ClubWebController(ClubRepository clubRepository, ClubEventRepository eventRepository, StudentRepository studentRepository) {
        this.clubRepository = clubRepository;
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/student-life")
    public String studentLife(Model model) {
        model.addAttribute("clubs", clubRepository.findAll());
        model.addAttribute("events", eventRepository.findAll());
        return "student-life";
    }

    @GetMapping("/club/{id}")
    public String clubDetail(@PathVariable Long id, Model model, Principal principal) {
        clubRepository.findById(id).ifPresent(club -> {
            model.addAttribute("club", club);
            model.addAttribute("events", eventRepository.findByClubId(id));
            if (principal != null) {
                boolean isMember = club.getMembers() != null && club.getMembers().stream()
                        .anyMatch(s -> s.getEmail().equals(principal.getName()));
                model.addAttribute("isMember", isMember);
                boolean isPresident = principal.getName().equals(club.getPresidentEmail());
                model.addAttribute("isPresident", isPresident);
            }
        });
        return "club-detail";
    }

    @PostMapping("/club/{id}/join")
    public String joinClub(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            clubRepository.findById(id).ifPresent(club -> {
                studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                    boolean alreadyMember = club.getMembers() != null && club.getMembers().stream()
                            .anyMatch(s -> s.getEmail().equals(principal.getName()));
                    if (!alreadyMember) {
                        club.getMembers().add(student);
                        clubRepository.save(club);
                        ra.addFlashAttribute("successMessage", "You have joined " + club.getName() + "!");
                    }
                });
            });
        }
        return "redirect:/club/" + id;
    }

    @PostMapping("/club/{id}/leave")
    public String leaveClub(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            clubRepository.findById(id).ifPresent(club -> {
                club.getMembers().removeIf(s -> s.getEmail().equals(principal.getName()));
                clubRepository.save(club);
                ra.addFlashAttribute("successMessage", "You have left " + club.getName() + ".");
            });
        }
        return "redirect:/club/" + id;
    }

    // Admin: Create Club
    @GetMapping("/admin/clubs")
    public String adminClubs(Model model) {
        model.addAttribute("clubs", clubRepository.findAll());
        return "admin-clubs";
    }

    @PostMapping("/admin/clubs/create")
    public String createClub(@RequestParam String name, @RequestParam String description, @RequestParam String category, RedirectAttributes ra) {
        Club club = new Club();
        club.setName(name);
        club.setDescription(description);
        club.setCategory(category);
        clubRepository.save(club);
        ra.addFlashAttribute("successMessage", "Club '" + name + "' created!");
        return "redirect:/admin/clubs";
    }

    @PostMapping("/club/{clubId}/event")
    public String createEvent(@PathVariable Long clubId, @RequestParam String title, @RequestParam String description,
                              @RequestParam String eventDate, @RequestParam(required = false) String eventTime,
                              @RequestParam(required = false) String location, RedirectAttributes ra) {
        clubRepository.findById(clubId).ifPresent(club -> {
            ClubEvent event = new ClubEvent();
            event.setClub(club);
            event.setTitle(title);
            event.setDescription(description);
            event.setEventDate(LocalDate.parse(eventDate));
            if (eventTime != null && !eventTime.isEmpty()) {
                event.setEventTime(LocalTime.parse(eventTime));
            }
            event.setLocation(location);
            eventRepository.save(event);
            ra.addFlashAttribute("successMessage", "Event created!");
        });
        return "redirect:/club/" + clubId;
    }
}
