package com._Person.university_system.operations;

import com._Person.university_system.users.Student;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "clubs")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String presidentEmail;

    @ManyToMany
    @JoinTable(
        name = "club_members",
        joinColumns = @JoinColumn(name = "club_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> members;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventDate ASC")
    private List<ClubEvent> events;

    public Club() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPresidentEmail() { return presidentEmail; }
    public void setPresidentEmail(String presidentEmail) { this.presidentEmail = presidentEmail; }
    public List<Student> getMembers() { return members; }
    public void setMembers(List<Student> members) { this.members = members; }
    public List<ClubEvent> getEvents() { return events; }
    public void setEvents(List<ClubEvent> events) { this.events = events; }
}
