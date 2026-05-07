package com._Person.university_system.controllers;

import com._Person.university_system.AcademicStructure.Schedule;
import com._Person.university_system.repositories.ScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    public ScheduleController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping
    public List<Schedule> getAll() {
        return scheduleRepository.findAll();
    }

    @GetMapping("/day/{day}")
    public List<Schedule> getByDay(@PathVariable String day) {
        return scheduleRepository.findByDayOfWeek(day);
    }

    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable Long id, @RequestBody Schedule updated) {
        return scheduleRepository.findById(id).map(s -> {
            s.setDayOfWeek(updated.getDayOfWeek());
            s.setStartTime(updated.getStartTime());
            s.setEndTime(updated.getEndTime());
            s.setRoom(updated.getRoom());
            s.setCourse(updated.getCourse());
            s.setLecturer(updated.getLecturer());
            return ResponseEntity.ok(scheduleRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
