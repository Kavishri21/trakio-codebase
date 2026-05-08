package kanban_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    
    private String recipientId; // User who receives notification
    private String senderId; // User who triggered the notification
    private String senderName;
    private String type; // TASK_ASSIGNED, TEAM_ADDED, MENTION
    private String message;
    private String relatedId; // taskId or teamId
    private boolean isRead = false;
    private Instant createdAt;
}
