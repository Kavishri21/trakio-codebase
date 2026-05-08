package kanban_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    private String taskID;       // Human-readable ID e.g., "MY1", "TFR2"

    private String title;
    private String description;
    private String status;       
    private String reason;
    private String priority;     
    private Instant dueDate;

    private Instant createdAt;
    private Instant updatedAt;   

    private List<StatusHistory> statusHistory = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private String teamId;          // The team this task belongs to
    private String userId;          // The assigned user
    private String createdByUserId; // Who created/assigned this task

    private Double position;        // Order within priority group


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private String status;
        private Instant changedAt;
        private String changedBy;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Comment {
        private String id;
        private String authorId;
        private String authorName;
        private String text;
        private Instant createdAt;
        private List<String> mentionedUserIds = new ArrayList<>();
        private List<String> readBy = new ArrayList<>(); // User IDs who have read this comment/mention
    }
}
