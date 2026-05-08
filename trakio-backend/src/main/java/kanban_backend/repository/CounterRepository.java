package kanban_backend.repository;

import kanban_backend.model.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository extends MongoRepository<Counter, String> {
    // ID is the prefix string — MongoRepository handles findById and save natively
}
