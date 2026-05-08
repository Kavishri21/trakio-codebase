package kanban_backend.service;

import kanban_backend.model.Invitation;
import kanban_backend.model.User;
import kanban_backend.repository.InvitationRepository;
import kanban_backend.repository.UserRepository;
import kanban_backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public InvitationService(InvitationRepository invitationRepository,
                             UserRepository userRepository,
                             EmailService emailService,
                             PasswordEncoder passwordEncoder,
                             JwtUtil jwtUtil) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // --- Send Invite ---
    public Map<String, Object> createInvitation(String email, String name, String inviterEmail, String inviterName) {
        // 1. Block if already a registered user
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("A user with this email is already a member.");
        }

        // 2. Inactivate previous unused invites for this email
        java.util.List<Invitation> existingInvites = invitationRepository.findByEmailAndUsedFalse(email);
        for (Invitation inv : existingInvites) {
            inv.setUsed(true); // Effectively invalidates the old token
        }
        invitationRepository.saveAll(existingInvites);

        // 3. Generate UUID token and save new invite
        String token = UUID.randomUUID().toString();
        Invitation invitation = new Invitation();
        invitation.setToken(token);
        invitation.setEmail(email);
        invitation.setName(name);
        invitation.setInvitedBy(inviterEmail);
        invitation.setInviterName(inviterName);
        invitation.setCreatedAt(Instant.now());
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setUsed(false);
        invitationRepository.save(invitation);

        // 4. Send the email
        emailService.sendInviteEmail(email, name, inviterName, token);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invitation sent successfully to " + email);
        return response;
    }

    // --- Validate Token (called when recipient opens the link) ---
    public Map<String, Object> validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation link."));

        if (invitation.isUsed()) {
            throw new RuntimeException("This invitation has already been used.");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("This invitation link has expired.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("email", invitation.getEmail());
        response.put("name", invitation.getName());
        response.put("valid", true);
        return response;
    }

    // --- Accept Invite: set password, create user, auto-login ---
    public Map<String, Object> acceptInvitation(String token, String password) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation link."));

        if (invitation.isUsed()) {
            throw new RuntimeException("This invitation has already been used.");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("This invitation link has expired.");
        }

        // Create the new User
        User user = new User();
        user.setName(invitation.getName());
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Mark invite as consumed
        invitation.setUsed(true);
        invitationRepository.save(invitation);

        // Auto-login: return JWT so React logs them in immediately
        String jwtToken = jwtUtil.generateToken(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("user", user);
        return response;
    }
}
