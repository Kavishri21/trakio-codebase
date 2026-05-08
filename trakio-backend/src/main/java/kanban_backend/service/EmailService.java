package kanban_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendInviteEmail(String toEmail, String toName, String inviterName, String token) {
        String inviteLink = frontendUrl + "/accept-invite?token=" + token;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 32px;'>"
            + "<h2 style='color: #1d4ed8; margin-bottom: 8px;'>You're invited to join Trakio!</h2>"
            + "<p style='color: #374151;'>Hi <strong>" + toName + "</strong>,</p>"
            + "<p style='color: #374151;'>You've been invited by <strong>" + inviterName + "</strong> to join the team on Trakio.</p>"
            + "<a href='" + inviteLink + "' style='display: inline-block; background: #2563eb; color: white; "
            + "padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: bold; "
            + "font-size: 15px; margin: 20px 0;'>Set password &amp; join</a>"
            + "<p style='color: #6b7280; font-size: 14px;'>Or copy this link:</p>"
            + "<p style='color: #2563eb; font-size: 13px; word-break: break-all;'>" + inviteLink + "</p>"
            + "<p style='color: #9ca3af; font-size: 13px; margin-top: 24px;'>This link expires in 7 days.</p>"
            + "</div>";

        try {
            // Build request body as a proper Map so Jackson handles all escaping
            Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Trakio", "email", "dr.kavi21k@gmail.com"),
                "to", List.of(Map.of("email", toEmail, "name", toName)),
                "subject", "You're invited to join Trakio",
                "htmlContent", htmlContent
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("Brevo API error: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send invite email: " + e.getMessage(), e);
        }
    }

    public void sendTaskAssignmentEmail(String toEmail, String toName,
                                         String assignerName, String taskTitle,
                                         String priority, String teamName) {
        String boardUrl = frontendUrl;
        String priorityColor = "high".equalsIgnoreCase(priority) ? "#ef4444"
                             : "medium".equalsIgnoreCase(priority) ? "#f59e0b"
                             : "#10b981";

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 32px; background: #f8fafc; border-radius: 12px;'>"
            + "<div style='background: white; border-radius: 10px; padding: 32px; border: 1px solid #e2e8f0;'>"
            + "<h2 style='color: #1d4ed8; margin-bottom: 4px; font-size: 20px;'>📋 New Task Assigned</h2>"
            + "<p style='color: #64748b; margin-top: 0; font-size: 14px;'>Trakio Notification</p>"
            + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;'/>"
            + "<p style='color: #374151; font-size: 15px;'>Hi <strong>" + toName + "</strong>,</p>"
            + "<p style='color: #374151; font-size: 15px;'><strong>" + assignerName + "</strong> has assigned a new task to you.</p>"
            + "<div style='background: #f1f5f9; border-left: 4px solid #2563eb; border-radius: 6px; padding: 16px 20px; margin: 20px 0;'>"
            + "<p style='margin: 0 0 8px 0; font-size: 13px; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; font-weight: bold;'>Task</p>"
            + "<p style='margin: 0 0 12px 0; font-size: 17px; font-weight: bold; color: #1e293b;'>" + taskTitle + "</p>"
            + "<p style='margin: 0 0 4px 0; font-size: 13px; color: #64748b;'>Priority: <span style='font-weight: bold; color: " + priorityColor + ";'>" + priority.substring(0,1).toUpperCase() + priority.substring(1) + "</span></p>"
            + "<p style='margin: 0; font-size: 13px; color: #64748b;'>Team: <strong style='color: #374151;'>" + teamName + "</strong></p>"
            + "</div>"
            + "<a href='" + boardUrl + "' style='display: inline-block; background: #2563eb; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 15px; margin-top: 8px;'>View Your Board →</a>"
            + "<p style='color: #9ca3af; font-size: 13px; margin-top: 28px;'>You are receiving this because you are a member of " + teamName + " on Trakio.</p>"
            + "</div></div>";

        try {
            Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Trakio", "email", "dr.kavi21k@gmail.com"),
                "to", List.of(Map.of("email", toEmail, "name", toName)),
                "subject", "New task assigned to you: " + taskTitle,
                "htmlContent", htmlContent
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                // Log but don't throw — task creation must not be blocked by email failure
                System.err.println("Task assignment email failed (Brevo): " + response.body());
            }

        } catch (Exception e) {
            // Email is optional — log error, never block task saving
            System.err.println("Failed to send task assignment email: " + e.getMessage());
        }
    }
    public void sendTeamAdditionEmail(String toEmail, String toName, String requesterName, String teamName) {
        String boardUrl = frontendUrl;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 32px; background: #f8fafc; border-radius: 12px;'>"
            + "<div style='background: white; border-radius: 10px; padding: 32px; border: 1px solid #e2e8f0;'>"
            + "<h2 style='color: #1d4ed8; margin-bottom: 4px; font-size: 20px;'>🚀 You've been added to a Team</h2>"
            + "<p style='color: #64748b; margin-top: 0; font-size: 14px;'>Trakio Notification</p>"
            + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;'/>"
            + "<p style='color: #374151; font-size: 15px;'>Hi <strong>" + toName + "</strong>,</p>"
            + "<p style='color: #374151; font-size: 15px;'>You have been added to the team <strong>" + teamName + "</strong> by <strong>" + requesterName + "</strong>.</p>"
            + "<div style='background: #f1f5f9; border-left: 4px solid #2563eb; border-radius: 6px; padding: 16px 20px; margin: 20px 0; text-align: center;'>"
            + "<p style='margin: 0; font-size: 16px; color: #1e293b;'>You can now access tasks and collaborate within <strong>" + teamName + "</strong>.</p>"
            + "</div>"
            + "<a href='" + boardUrl + "' style='display: inline-block; background: #2563eb; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 15px; margin-top: 8px;'>Go to Dashboard →</a>"
            + "<p style='color: #9ca3af; font-size: 13px; margin-top: 28px;'>Manage your tasks and teams efficiently on your Trakio.</p>"
            + "</div></div>";

        try {
            Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Trakio", "email", "dr.kavi21k@gmail.com"),
                "to", List.of(Map.of("email", toEmail, "name", toName)),
                "subject", "You've been added to a new team: " + teamName,
                "htmlContent", htmlContent
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                System.err.println("Team addition email failed (Brevo): " + response.body());
            }

        } catch (Exception e) {
            System.err.println("Failed to send team addition email: " + e.getMessage());
        }
    }
}

