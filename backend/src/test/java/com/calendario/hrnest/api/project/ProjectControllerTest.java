package com.calendario.hrnest.api.project;

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
class ProjectControllerTest {

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

    private String createUserAndGetToken(String email, Role role) {
        User user = User.reconstitute(null, email, "hash", "Ala", "Kadrowa", role, Instant.now());
        User saved = userRepository.save(user);
        return tokenProvider.generateToken(saved);
    }

    @Test
    void create_asHr_thenListAll_includesIt() throws Exception {
        String hrToken = createUserAndGetToken("hrproj1@example.com", Role.HR);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Kalendario","description":"Aplikacja HR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kalendario"));

        mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_asEmployee_isForbidden() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("empproj1@example.com");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Kalendario"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_duplicateName_isConflict() throws Exception {
        String hrToken = createUserAndGetToken("hrproj2@example.com", Role.HR);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplikat"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplikat"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void summary_asEmployee_isForbidden() throws Exception {
        String hrToken = createUserAndGetToken("hrproj3@example.com", Role.HR);
        String createResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"ProjektX"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        String employeeToken = registerEmployeeAndGetToken("empproj2@example.com");

        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void summary_unknownProject_isNotFound() throws Exception {
        String hrToken = createUserAndGetToken("hrproj4@example.com", Role.HR);

        mockMvc.perform(get("/api/projects/999999/summary").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isNotFound());
    }
}
