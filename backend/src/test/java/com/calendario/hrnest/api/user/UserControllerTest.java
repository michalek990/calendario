package com.calendario.hrnest.api.user;

import static org.hamcrest.Matchers.nullValue;
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
class UserControllerTest {

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
        User user = User.reconstitute(null, email, "hash", "Ala", "Szefowa", role, Instant.now());
        User saved = userRepository.save(user);
        return new RegisteredUser(saved.getId(), tokenProvider.generateToken(saved));
    }

    @Test
    void myProfile_returnsDefaultProfile_forNewlyRegisteredUser() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("profil1@example.com");

        mockMvc.perform(get("/api/users/me/profile").header("Authorization", "Bearer " + employee.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.position").value(nullValue()))
                .andExpect(jsonPath("$.hasSupervisor").value(false))
                .andExpect(jsonPath("$.isSupervisor").value(false));
    }

    @Test
    void updateProfile_asHrAdmin_setsOrganizationalData() throws Exception {
        RegisteredUser hrAdmin = createUserAndGetToken("hr1@example.com", Role.HR_ADMIN);
        RegisteredUser supervisor = createUserAndGetToken("kierownik1@example.com", Role.MANAGER);
        RegisteredUser employee = registerEmployeeAndGetToken("profil2@example.com");

        mockMvc.perform(patch("/api/users/" + employee.id() + "/profile")
                        .header("Authorization", "Bearer " + hrAdmin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position":"Programista","department":"IT","facility":"Warszawa","supervisorId":%d}
                                """.formatted(supervisor.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Programista"))
                .andExpect(jsonPath("$.department").value("IT"))
                .andExpect(jsonPath("$.facility").value("Warszawa"))
                .andExpect(jsonPath("$.hasSupervisor").value(true))
                .andExpect(jsonPath("$.supervisorFullName").value("Ala Szefowa"));

        mockMvc.perform(get("/api/users/me/profile").header("Authorization", "Bearer " + supervisor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSupervisor").value(true));
    }

    @Test
    void updateProfile_asEmployee_isForbidden() throws Exception {
        RegisteredUser employee = registerEmployeeAndGetToken("profil3@example.com");

        mockMvc.perform(patch("/api/users/" + employee.id() + "/profile")
                        .header("Authorization", "Bearer " + employee.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position":"Programista","department":"IT","facility":"Warszawa"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_selfSupervision_isRejected() throws Exception {
        RegisteredUser hrAdmin = createUserAndGetToken("hr2@example.com", Role.HR_ADMIN);
        RegisteredUser employee = registerEmployeeAndGetToken("profil4@example.com");

        mockMvc.perform(patch("/api/users/" + employee.id() + "/profile")
                        .header("Authorization", "Bearer " + hrAdmin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position":"X","department":"Y","facility":"Z","supervisorId":%d}
                                """.formatted(employee.id())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void myProfile_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me/profile")).andExpect(status().isForbidden());
    }
}
