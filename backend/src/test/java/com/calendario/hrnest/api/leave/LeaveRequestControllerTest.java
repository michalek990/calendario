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

    private String registerEmployeeAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "bardzoSilneHaslo123", "Jan", "Kowalski");
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String createManagerAndGetToken(String email) {
        User manager = User.reconstitute(null, email, "hash", "Ala", "Manager", Role.MANAGER, Instant.now());
        User saved = userRepository.save(manager);
        return tokenProvider.generateToken(saved);
    }

    @Test
    void createLeaveRequest_thenListMine_returnsIt() throws Exception {
        String token = registerEmployeeAndGetToken("pracownik1@example.com");

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
        String token = registerEmployeeAndGetToken("pracownik2@example.com");

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VACATION","startDate":"2026-08-07","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_asManager_succeeds() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("pracownik3@example.com");
        String managerToken = createManagerAndGetToken("manager1@example.com");

        String createResponse = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"SICK_LEAVE","startDate":"2026-08-03","endDate":"2026-08-03"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/api/leave-requests/" + id + "/approve").header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_asEmployee_isForbidden() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("pracownik4@example.com");

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
        String employeeToken = registerEmployeeAndGetToken("pracownik5@example.com");

        mockMvc.perform(get("/api/leave-requests/pending").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpoints_requireAuthentication() throws Exception {
        // Brak jawnego AuthenticationEntryPoint w SecurityConfig -> domyślnie Spring Security
        // odrzuca anonimowe żądanie do chronionego zasobu z 403, nie 401.
        mockMvc.perform(get("/api/leave-requests/me")).andExpect(status().isForbidden());
    }
}
