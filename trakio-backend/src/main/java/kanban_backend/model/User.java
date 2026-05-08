package kanban_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Allow input (signup/login), hide output (JSON responses)
    private String password;

    private Boolean active = true; // Use Boolean object to handle nulls from old data

    private String globalRole = "EMPLOYEE"; // ORG_ADMIN, MANAGER, EMPLOYEE

    public boolean isActive() {
        return active == null || active;
    }
}


