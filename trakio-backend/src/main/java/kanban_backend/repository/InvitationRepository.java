package kanban_backend.repository;

import kanban_backend.model.Invitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InvitationRepository extends MongoRepository<Invitation, String> {

    // Find by the UUID token (used when recipient clicks the link)
    Optional<Invitation> findByToken(String token);

    // Find all pending (unused) invites for this email
    java.util.List<Invitation> findByEmailAndUsedFalse(String email);
}
