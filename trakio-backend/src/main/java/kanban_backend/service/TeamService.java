package kanban_backend.service;

import kanban_backend.model.Team;
import kanban_backend.model.User;
import kanban_backend.model.Task;
import kanban_backend.repository.TeamRepository;
import kanban_backend.repository.UserRepository;
import kanban_backend.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository, EmailService emailService, NotificationService notificationService, TaskRepository taskRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;
    }

    public List<Team> getAllTeams(String requesterEmail) {
        if (requesterEmail == null) {
             return teamRepository.findAll();
        }
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if ("ORG_ADMIN".equals(requester.getGlobalRole())) {
            return teamRepository.findAll();
        }
        
        // MANAGER and EMPLOYEE only see teams where they are members
        return teamRepository.findByMembersUserId(requester.getId());
    }

    public Team createTeam(String name, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        if ("EMPLOYEE".equals(creator.getGlobalRole())) {
            throw new RuntimeException("Employees cannot create teams.");
        }

        Team team = new Team();
        team.setName(name);
        team.setCreatedByUserId(creator.getId()); // Retain as origin log tracking
        
        // Add creator as LEAD
        team.getMembers().add(new Team.TeamMember(creator.getId(), "LEAD"));
        
        return teamRepository.save(team);
    }

    public Team addMemberToTeam(String teamId, String targetUserId, String teamRole, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isOrgAdmin = "ORG_ADMIN".equals(requester.getGlobalRole());
        boolean isLead = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(requester.getId()) && "LEAD".equals(m.getTeamRole()));
                
        if (!isOrgAdmin && !isLead) {
            throw new RuntimeException("Only Team Leads or Org Admins can add members.");
        }
        
        if (team.getMembers().stream().anyMatch(m -> m.getUserId().equals(targetUserId))) {
            throw new RuntimeException("User is already in the team.");
        }

        team.getMembers().add(new Team.TeamMember(targetUserId, teamRole));
        Team saved = teamRepository.save(team);

        // Send Notification Email
        try {
            if (!targetUserId.equals(requester.getId())) {
                User targetUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new RuntimeException("Target user not found"));
                
                emailService.sendTeamAdditionEmail(
                    targetUser.getEmail(),
                    targetUser.getName(),
                    requester.getName(),
                    team.getName()
                );
                
                // --- In-App Notification ---
                notificationService.createNotification(
                    targetUser.getId(),
                    requester.getId(),
                    requester.getName(),
                    "TEAM_ADDED",
                    "added you to the team: " + team.getName(),
                    team.getId()
                );
            }
        } catch (Exception e) {
            // Email is secondary — don't block the member addition
            System.err.println("Could not send team addition email: " + e.getMessage());
        }

        return saved;
    }
    
    public Team updateMemberRole(String teamId, String targetUserId, String newTeamRole, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isOrgAdmin = "ORG_ADMIN".equals(requester.getGlobalRole());
        boolean isLead = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(requester.getId()) && "LEAD".equals(m.getTeamRole()));
                
        if (!isOrgAdmin && !isLead) {
            throw new RuntimeException("Only Team Leads or Org Admins can update roles.");
        }

        // Rule 4: Last LEAD protection
        if ("CONTRIBUTOR".equals(newTeamRole)) {
            Team.TeamMember targetMember = team.getMembers().stream()
                .filter(m -> m.getUserId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found in team"));
                
            if ("LEAD".equals(targetMember.getTeamRole())) {
                long leadCount = team.getMembers().stream().filter(m -> "LEAD".equals(m.getTeamRole())).count();
                if (leadCount <= 1) {
                    throw new RuntimeException("Cannot remove the only team lead. Promote another member to Lead first.");
                }
            }
        }

        team.getMembers().forEach(m -> {
            if (m.getUserId().equals(targetUserId)) {
                m.setTeamRole(newTeamRole);
            }
        });

        return teamRepository.save(team);
    }

    public Team renameTeam(String teamId, String newName, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isOrgAdmin = "ORG_ADMIN".equals(requester.getGlobalRole());
        boolean isLead = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(requester.getId()) && "LEAD".equals(m.getTeamRole()));
                
        if (!isOrgAdmin && !isLead) {
            throw new RuntimeException("Only Team Leads or Org Admins can rename the team.");
        }

        team.setName(newName);
        return teamRepository.save(team);
    }

    public void removeMemberFromTeam(String teamId, String targetUserId, String requesterEmail) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isOrgAdmin = "ORG_ADMIN".equals(requester.getGlobalRole());
        boolean isLead = team.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(requester.getId()) && "LEAD".equals(m.getTeamRole()));
                
        if (!isOrgAdmin && !isLead) {
            throw new RuntimeException("Only Team Leads or Org Admins can remove members.");
        }

        Team.TeamMember targetMember = team.getMembers().stream()
            .filter(m -> m.getUserId().equals(targetUserId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Member not found in team"));

        if ("LEAD".equals(targetMember.getTeamRole())) {
            long leadCount = team.getMembers().stream().filter(m -> "LEAD".equals(m.getTeamRole())).count();
            if (leadCount <= 1) {
                throw new RuntimeException("Cannot remove the only team lead. Promote another member to Lead first.");
            }
        }

        team.getMembers().removeIf(m -> m.getUserId().equals(targetUserId));
        teamRepository.save(team);
    }

    public void deleteTeam(String teamId) {
        teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        
        // Convert all team tasks to personal tasks
        List<Task> teamTasks = taskRepository.findByTeamId(teamId);
        for (Task task : teamTasks) {
            task.setTeamId(null);
        }
        taskRepository.saveAll(teamTasks);

        teamRepository.deleteById(teamId);
    }
}
