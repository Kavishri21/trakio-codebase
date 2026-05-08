package kanban_backend.repository;

import kanban_backend.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface TeamRepository extends MongoRepository<Team, String> {
    // Find all teams that contain a specific userId in their members list
    @Query("{ 'members.userId': ?0 }")
    List<Team> findByMembersUserId(String userId);
}
