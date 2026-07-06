package com.calendario.hrnest.api.notification;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.calendario.hrnest.api.auth.RegisterRequest;
import com.calendario.hrnest.application.auth.TokenProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    private record RegisteredUser(Long id, String token) {
    }

    private RegisteredUser registerEmployeeAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "bardzoSilneHaslo123", "Jan", "Kowalski");
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();
        Long id = userRepository.findByEmail(email).orElseThrow().getId();
        return new RegisteredUser(id, token);
    }

    private RegisteredUser createUserAndGetToken(String email, Role role) {
        User user = User.reconstitute(null, email, "hash", "Ala", "Kadrowa", role, Instant.now());
        User saved = userRepository.save(user);
        return new RegisteredUser(saved.getId(), tokenProvider.generateToken(saved));
    }

    @Test
    void approvedLeaveRequest_createsNotificationForRequester_thenCanBeMarkedRead() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("notif1@example.com");
        RegisteredUser hr = createUserAndGetToken("hrnotif1@example.com", Role.HR);

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long leaveRequestId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + leaveRequestId + "/approve")
                        .header("Authorization", "Bearer " + hr.token()))
                .andExpect(status().isOk());

        String notificationsResponse = mockMvc.perform(get("/api/notifications/me")
                        .header("Authorization", "Bearer " + employee.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("LEAVE_REQUEST_APPROVED"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andReturn().getResponse().getContentAsString();
        Long notificationId = objectMapper.readTree(notificationsResponse).get(0).get("id").asLong();

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + employee.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAsRead_forSomeoneElsesNotification_isForbidden() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("notif2@example.com");
        RegisteredUser stranger = registerEmployeeAndGetToken("notif3@example.com");
        RegisteredUser hr = createUserAndGetToken("hrnotif2@example.com", Role.HR);

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"OTHER","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long leaveRequestId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + leaveRequestId + "/reject")
                        .header("Authorization", "Bearer " + hr.token()))
                .andExpect(status().isOk());

        String notificationsResponse = mockMvc.perform(get("/api/notifications/me")
                        .header("Authorization", "Bearer " + employee.token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long notificationId = objectMapper.readTree(notificationsResponse).get(0).get("id").asLong();

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + stranger.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void listMine_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/notifications/me")).andExpect(status().isForbidden());
    }
}
