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
@Document(collection = "invitations")
public class Invitation {

    @Id
    private String id;

    private String token;       // UUID invite token
    private String email;       // Recipient email
    private String name;        // Recipient name
    private String invitedBy;   // Inviter's email
    private String inviterName; // Inviter's display name

    private Instant createdAt;
    private Instant expiresAt;  // 7 days from creation
    private boolean used;       // true once the invite is accepted
}
