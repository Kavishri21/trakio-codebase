package kanban_backend.controller;

import kanban_backend.repository.UserRepository;
import kanban_backend.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserRepository userRepository;

    public InvitationController(InvitationService invitationService, UserRepository userRepository) {
        this.invitationService = invitationService;
        this.userRepository = userRepository;
    }

    // POST /api/invitations — Send an invite (requires login)
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendInvitation(
            @RequestBody Map<String, String> body,
            Principal principal) {

        String inviterEmail = principal.getName(); // from JWT
        String inviterName = userRepository.findByEmail(inviterEmail)
                .map(u -> u.getName())
                .orElse(inviterEmail);

        return ResponseEntity.ok(invitationService.createInvitation(
                body.get("email"),
                body.get("name"),
                inviterEmail,
                inviterName
        ));
    }

    // GET /api/invitations/validate?token=xyz — Validate token (public, no login needed)
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateInvitation(@RequestParam String token) {
        return ResponseEntity.ok(invitationService.validateToken(token));
    }

    // POST /api/invitations/accept — Accept invite + set password (public, no login needed)
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptInvitation(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(invitationService.acceptInvitation(
                body.get("token"),
                body.get("password")
        ));
    }
}
