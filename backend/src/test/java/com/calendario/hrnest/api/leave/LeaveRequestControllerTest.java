package com.calendario.hrnest.api.leave;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.calendario.hrnest.api.auth.RegisterRequest;
import com.calendario.hrnest.application.auth.TokenProvider;
import com.calendario.hrnest.domain.leave.LeaveType;
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
class LeaveRequestControllerTest {

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
        User user = User.reconstitute(null, email, "hash", "Ala", "Kierownik", role, Instant.now());
        User saved = userRepository.save(user);
        return new RegisteredUser(saved.getId(), tokenProvider.generateToken(saved));
    }

    private void assignSupervisor(Long employeeId, Long supervisorId) {
        User employee = userRepository.findById(employeeId).orElseThrow();
        userRepository.save(employee.updateOrganization(null, null, null, supervisorId));
    }

    @Test
    void createLeaveRequest_thenListMine_returnsIt() throws Exception {
        String token = registerEmployeeAndGetToken("pracownik1@example.com").token();

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-03","endDate":"2026-08-07","reason":"Wakacje"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.daysCount").value(5));

        mockMvc.perform(get("/api/leave-requests/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createLeaveRequest_rejectsInvalidRange() throws Exception {
        String token = registerEmployeeAndGetToken("pracownik2@example.com").token();

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-07","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_asDirectSupervisor_succeeds() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("pracownik3@example.com");
        RegisteredUser manager = createUserAndGetToken("manager1@example.com", Role.MANAGER);
        assignSupervisor(employee.id(), manager.id());

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"SICK_LEAVE","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + manager.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_asManagerWithoutSupervisorRelation_isForbidden() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("pracownik3b@example.com");
        RegisteredUser unrelatedManager = createUserAndGetToken("manager1b@example.com", Role.MANAGER);

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"SICK_LEAVE","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + unrelatedManager.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_asHr_succeedsWithoutSupervisorRelation() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("pracownik3c@example.com");
        RegisteredUser hr = createUserAndGetToken("hrleave1@example.com", Role.HR);

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"SICK_LEAVE","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + hr.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_asEmployee_isForbidden() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("pracownik4@example.com").token();

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"OTHER","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/approve").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPending_asEmployee_isForbidden() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("pracownik5@example.com").token();

        mockMvc.perform(get("/api/leave-requests/pending").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPending_asManager_onlyShowsDirectReports() throws Exception {
        RegisteredUser reportee = registerEmployeeAndGetToken("pracownik5b@example.com");
        RegisteredUser stranger = registerEmployeeAndGetToken("pracownik5c@example.com");
        RegisteredUser manager = createUserAndGetToken("manager2@example.com", Role.MANAGER);
        assignSupervisor(reportee.id(), manager.id());

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + reportee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + stranger.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-04","endDate":"2026-08-04"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/leave-requests/pending").header("Authorization", "Bearer " + manager.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].requesterId").value(reportee.id()));
    }

    @Test
    void endpoints_requireAuthentication() throws Exception {
        // Brak jawnego AuthenticationEntryPoint w SecurityConfig -> domyślnie Spring Security
        // odrzuca anonimowe żądanie do chronionego zasobu z 403, nie 401.
        mockMvc.perform(get("/api/leave-requests/me")).andExpect(status().isForbidden());
    }

    @Test
    void createLeaveRequest_acceptsRemoteWorkType() throws Exception {
        String token = registerEmployeeAndGetToken("pracownik6@example.com").token();

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"REMOTE_WORK","startDate":"2026-08-03","endDate":"2026-08-03","reason":"Praca z domu"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("REMOTE_WORK"));
    }

    @Test
    void recentActivity_returnsOwnRequests_mostRecentFirst() throws Exception {
        String token = registerEmployeeAndGetToken("pracownik7@example.com").token();

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"ON_DEMAND","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/leave-requests/me/recent-activity").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("ON_DEMAND"));
    }
}
