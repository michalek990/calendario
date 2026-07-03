package com.calendario.hrnest.api.timetracking;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.calendario.hrnest.api.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "bardzoSilneHaslo123", "Jan", "Kowalski");
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
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
}
