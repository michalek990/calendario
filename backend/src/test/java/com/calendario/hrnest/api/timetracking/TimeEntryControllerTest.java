package com.calendario.hrnest.api.timetracking;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class TimeEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "bardzoSilneHaslo123", "Jan", "Kowalski");
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String createUserAndGetToken(String email, Role role) {
        User user = User.reconstitute(null, email, "hash", "Ala", "Kadrowa", role, Instant.now());
        User saved = userRepository.save(user);
        return tokenProvider.generateToken(saved);
    }

    @Test
    void clockIn_thenClockOut_thenListMine() throws Exception {
        String token = registerAndGetToken("czaspracy1@example.com");

        mockMvc.perform(post("/api/time-entries/clock-in").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clockOut").doesNotExist());

        mockMvc.perform(post("/api/time-entries/clock-out").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").exists());

        mockMvc.perform(get("/api/time-entries/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void clockIn_twice_returnsConflict() throws Exception {
        String token = registerAndGetToken("czaspracy2@example.com");

        mockMvc.perform(post("/api/time-entries/clock-in").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/time-entries/clock-in").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void clockOut_withoutOpenEntry_returnsConflict() throws Exception {
        String token = registerAndGetToken("czaspracy3@example.com");

        mockMvc.perform(post("/api/time-entries/clock-out").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void endpoints_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/time-entries/me")).andExpect(status().isForbidden());
    }

    @Test
    void clockIn_withUnknownProjectId_returnsNotFound() throws Exception {
        String token = registerAndGetToken("czaspracy4@example.com");

        mockMvc.perform(post("/api/time-entries/clock-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void clockIn_withProjectId_thenClockOut_thenByProjectSummary_showsTotal() throws Exception {
        String hrToken = createUserAndGetToken("hrczas1@example.com", Role.HR);
        String createProjectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"ProjektCzasu"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long projectId = objectMapper.readTree(createProjectResponse).get("id").asLong();

        String employeeToken = registerAndGetToken("czaspracy5@example.com");

        mockMvc.perform(post("/api/time-entries/clock-in")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":" + projectId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId));

        mockMvc.perform(post("/api/time-entries/clock-out").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/time-entries/me/by-project").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[0].projectName").value("ProjektCzasu"));

        mockMvc.perform(get("/api/projects/" + projectId + "/summary").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryCount").value(1));
    }

    @Test
    void listMineByProject_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/time-entries/me/by-project")).andExpect(status().isForbidden());
    }

    @Test
    void log_createsClosedEntry_forGivenInstants() throws Exception {
        String token = registerAndGetToken("czaspracy6@example.com");

        mockMvc.perform(post("/api/time-entries/log")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn":"2026-08-03T08:00:00Z","clockOut":"2026-08-03T16:00:00Z","breakMinutes":30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clockOut").exists())
                .andExpect(jsonPath("$.totalMinutes").value(8 * 60 - 30));
    }

    @Test
    void log_withEndBeforeStart_returnsBadRequest() throws Exception {
        String token = registerAndGetToken("czaspracy7@example.com");

        mockMvc.perform(post("/api/time-entries/log")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn":"2026-08-03T16:00:00Z","clockOut":"2026-08-03T08:00:00Z"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void log_withUnknownProjectId_returnsNotFound() throws Exception {
        String token = registerAndGetToken("czaspracy8@example.com");

        mockMvc.perform(post("/api/time-entries/log")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn":"2026-08-03T08:00:00Z","clockOut":"2026-08-03T16:00:00Z","projectId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void log_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/time-entries/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn":"2026-08-03T08:00:00Z","clockOut":"2026-08-03T16:00:00Z"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void listManaged_asHr_returnsEntriesWithEmployeeNames() throws Exception {
        String employeeToken = registerAndGetToken("czaspracy9@example.com");
        mockMvc.perform(post("/api/time-entries/clock-in").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/time-entries/clock-out").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        String hrToken = createUserAndGetToken("hrczas2@example.com", Role.HR);

        mockMvc.perform(get("/api/time-entries").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userFirstName").value("Jan"))
                .andExpect(jsonPath("$[0].userLastName").value("Kowalski"));
    }

    @Test
    void listManaged_asEmployee_returnsForbidden() throws Exception {
        String token = registerAndGetToken("czaspracy10@example.com");

        mockMvc.perform(get("/api/time-entries").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listManaged_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/time-entries")).andExpect(status().isForbidden());
    }
}
