package kanban_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stores an auto-incrementing sequence for each task ID prefix (e.g., "MY", "TFR").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "task_id_counters")
public class Counter {

    @Id
    private String id; // format: "PREFIX:CONTEXT_ID" (e.g., "MY:userid" or "DEV:teamid")

    private String prefix;    // e.g. "MY", "TFR", "DEV"
    private String contextId; // e.g. userId or teamId

    private long seq;
}
