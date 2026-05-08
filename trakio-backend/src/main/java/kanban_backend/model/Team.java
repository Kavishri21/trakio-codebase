package kanban_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "teams")
@Data
@NoArgsConstructor
public class Team {
    @Id
    private String id;
    private String name;
    private String createdByUserId;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMember {
        private String userId;
        private String teamRole; // "LEAD" | "CONTRIBUTOR"
    }

    private List<TeamMember> members = new ArrayList<>();
}
