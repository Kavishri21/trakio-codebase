package kanban_backend.service;

import kanban_backend.model.Task;
import kanban_backend.model.User;
import kanban_backend.model.Team;
import kanban_backend.repository.TaskRepository;
import kanban_backend.repository.UserRepository;
import kanban_backend.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    public UserService(UserRepository userRepository, TaskRepository taskRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
    }

    private void verifyOrgAdmin(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!"ORG_ADMIN".equals(requester.getGlobalRole())) {
            throw new RuntimeException("Access Denied: Only ORG_ADMIN can perform this action.");
        }
    }

    public User updateGlobalRole(String id, String newRole, String requesterEmail) {
        verifyOrgAdmin(requesterEmail);
        
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Prevent removing the last ORG_ADMIN
        if ("ORG_ADMIN".equals(user.getGlobalRole()) && !"ORG_ADMIN".equals(newRole)) {
            long adminCount = userRepository.findByGlobalRole("ORG_ADMIN").size();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot demote the last ORG_ADMIN. Promote another user first.");
            }
        }
        
        user.setGlobalRole(newRole);
        return userRepository.save(user);
    }

    public User toggleUserStatus(String id, String requesterEmail) {
        verifyOrgAdmin(requesterEmail);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public void deleteUser(String id, String requesterEmail) {
        verifyOrgAdmin(requesterEmail);
        
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Prevent deleting the last ORG_ADMIN
        if ("ORG_ADMIN".equals(user.getGlobalRole())) {
            long adminCount = userRepository.findByGlobalRole("ORG_ADMIN").size();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last ORG_ADMIN.");
            }
        }
        
        // Unassign all tasks from this user before deleting them
        List<Task> userTasks = taskRepository.findByUserId(id);
        for (Task task : userTasks) {
            task.setUserId(null); // Mark as unassigned
        }
        taskRepository.saveAll(userTasks);

        // Remove user from all teams they belong to
        List<Team> userTeams = teamRepository.findByMembersUserId(id);
        for (Team team : userTeams) {
            Team.TeamMember member = team.getMembers().stream()
                .filter(m -> m.getUserId().equals(id))
                .findFirst()
                .orElse(null);
                
            if (member != null && "LEAD".equals(member.getTeamRole())) {
                long leadCount = team.getMembers().stream().filter(m -> "LEAD".equals(m.getTeamRole())).count();
                if (leadCount <= 1) {
                    throw new RuntimeException("Cannot delete user. They are the only LEAD of team: '" + team.getName() + "'. Promote another member to LEAD first.");
                }
            }
            team.getMembers().removeIf(m -> m.getUserId().equals(id));
        }
        teamRepository.saveAll(userTeams);

        userRepository.deleteById(id);
    }
}
