package com.calendario.hrnest.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.calendario.hrnest.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        // każdy test rejestruje unikalny e-mail, więc nie ma wspólnego stanu do czyszczenia
    }

    @Test
    void register_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "jan.kowalski@example.com", "bardzoSilneHaslo123", "Jan", "Kowalski");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())));

        assertThat(userRepository.existsByEmail("jan.kowalski@example.com")).isTrue();
    }

    @Test
    void register_rejectsDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "duplikat@example.com", "bardzoSilneHaslo123", "A", "B");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", not(emptyString())));
    }

    @Test
    void register_rejectsInvalidPayload() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "short", "", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returnsToken_forValidCredentials() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "login.test@example.com", "bardzoSilneHaslo123", "Login", "Test");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("login.test@example.com", "bardzoSilneHaslo123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    void login_returnsUnauthorized_forWrongPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "wrongpass@example.com", "bardzoSilneHaslo123", "A", "B");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("wrongpass@example.com", "zleHaslo");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returnsUnauthorized_forUnknownEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nieistnieje@example.com", "cokolwiek123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_updatesPassword_whenCurrentPasswordCorrect() throws Exception {
        String token = registerAndGetToken("change.pass1@example.com", "staraHaslo123");

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("staraHaslo123", "nowaHaslo456");
        mockMvc.perform(patch("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isNoContent());

        LoginRequest loginWithNewPassword = new LoginRequest("change.pass1@example.com", "nowaHaslo456");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginWithNewPassword)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_returnsUnauthorized_whenCurrentPasswordWrong() throws Exception {
        String token = registerAndGetToken("change.pass2@example.com", "staraHaslo123");

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("zlehaslo", "nowaHaslo456");
        mockMvc.perform(patch("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_requiresAuthentication() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest("staraHaslo123", "nowaHaslo456");

        mockMvc.perform(patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password, "Jan", "Kowalski");
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
