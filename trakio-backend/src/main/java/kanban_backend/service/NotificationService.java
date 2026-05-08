package kanban_backend.service;

import kanban_backend.model.Notification;
import kanban_backend.model.User;
import kanban_backend.repository.NotificationRepository;
import kanban_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void createNotification(String recipientId, String senderId, String senderName, String type, String message, String relatedId) {
        // Don't notify yourself
        if (recipientId.equals(senderId)) return;

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    public Notification markAsRead(String id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
                
        if (!notification.getRecipientId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized: You can only mark your own notifications as read.");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
    
    public void deleteNotification(String id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
                
        if (!notification.getRecipientId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized: You can only delete your own notifications.");
        }

        notificationRepository.delete(notification);
    }

    public void clearAllNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        notificationRepository.deleteByRecipientId(user.getId());
    }
}
