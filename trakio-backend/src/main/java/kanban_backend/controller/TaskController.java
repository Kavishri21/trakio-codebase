package kanban_backend.controller;

import kanban_backend.model.Task;
import kanban_backend.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false, defaultValue = "false") boolean createdByMe,
            Principal principal) {
        return ResponseEntity.ok(taskService.getTasks(principal.getName(), teamId, targetUserId, createdByMe));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, Principal principal) {
        Task created = taskService.createTask(task, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            Principal principal) {

        String newStatus = (String) body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        Double newPosition = null;
        if (body.containsKey("position")) {
            Object pos = body.get("position");
            if (pos instanceof Number) {
                newPosition = ((Number) pos).doubleValue();
            }
        }

        Task updated = taskService.updateStatus(id, newStatus, newPosition, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String id,
            @RequestBody Task task) {

        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Task> addComment(
            @PathVariable String id,
            @RequestBody Task.Comment comment,
            Principal principal) {
        return ResponseEntity.ok(taskService.addComment(id, comment, principal.getName()));
    }

    @PatchMapping("/{taskId}/comments/{commentId}/read")
    public ResponseEntity<Task> markCommentAsRead(
            @PathVariable String taskId,
            @PathVariable String commentId,
            Principal principal) {
        return ResponseEntity.ok(taskService.markCommentAsRead(taskId, commentId, principal.getName()));
    }

    @PutMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<Task> updateComment(
            @PathVariable String taskId,
            @PathVariable String commentId,
            @RequestBody Task.Comment comment,
            Principal principal) {
        return ResponseEntity.ok(taskService.updateComment(taskId, commentId, comment, principal.getName()));
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<Task> deleteComment(
            @PathVariable String taskId,
            @PathVariable String commentId,
            Principal principal) {
        return ResponseEntity.ok(taskService.deleteComment(taskId, commentId, principal.getName()));
    }
}
