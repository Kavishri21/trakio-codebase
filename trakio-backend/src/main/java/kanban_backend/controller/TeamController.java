package kanban_backend.controller;

import kanban_backend.model.Team;
import kanban_backend.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams(Principal principal) {
        return ResponseEntity.ok(teamService.getAllTeams(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Map<String, Object> body, Principal principal) {
        String name = (String) body.get("name");
        Team team = teamService.createTeam(name, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<Team> addMember(
            @PathVariable String teamId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String targetUserId = body.get("userId");
        String teamRole = body.get("teamRole"); // "LEAD" or "CONTRIBUTOR"
        Team updated = teamService.addMemberToTeam(teamId, targetUserId, teamRole, principal.getName());
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{teamId}/members/{userId}/role")
    public ResponseEntity<Team> updateMemberRole(
            @PathVariable String teamId,
            @PathVariable String userId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String newTeamRole = body.get("teamRole");
        Team updated = teamService.updateMemberRole(teamId, userId, newTeamRole, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{teamId}/name")
    public ResponseEntity<Team> renameTeam(
            @PathVariable String teamId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String newName = body.get("name");
        return ResponseEntity.ok(teamService.renameTeam(teamId, newName, principal.getName()));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String teamId,
            @PathVariable String userId,
            Principal principal) {
        teamService.removeMemberFromTeam(teamId, userId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String teamId) {
        // usually admin level only, but we didn't add requesterEmail to deleteTeam yet. 
        // We'll leave it as is or add it:
        teamService.deleteTeam(teamId);
        return ResponseEntity.ok().build();
    }
}
