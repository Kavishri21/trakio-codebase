package kanban_backend.repository;

import kanban_backend.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    java.util.List<Task> findByUserId(String userId);
    java.util.List<Task> findByTeamId(String teamId);
}
