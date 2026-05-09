package com._Person.university_system.services;

import com._Person.university_system.operations.Notification;
import com._Person.university_system.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    public void sendNotification(Long userId, String userRole, 
                                  String title, String message, String type) {
        Notification n = new Notification(title, message, type, userId, userRole);
        notificationRepository.save(n);
    }
    
    public void sendToAllStudents(List<Long> studentIds, 
                                   String title, String message, String type) {
        for (Long id : studentIds) {
            sendNotification(id, "STUDENT", title, message, type);
        }
    }
}
