package com.calendario.hrnest.api.facility;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class FacilityControllerTest {

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
    void create_asAdmin_thenListAll_includesIt() throws Exception {
        String adminToken = createUserAndGetToken("adminfac1@example.com", Role.ADMIN);

        mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Warszawa Fac1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Warszawa Fac1"));

        mockMvc.perform(get("/api/facilities").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Warszawa Fac1")));
    }

    @Test
    void create_asHr_isForbidden() throws Exception {
        String hrToken = createUserAndGetToken("hrfac1@example.com", Role.HR);

        mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Warszawa Fac2"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asEmployee_isForbidden() throws Exception {
        String employeeToken = registerEmployeeAndGetToken("empfac1@example.com");

        mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Warszawa Fac3"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_duplicateName_isConflict() throws Exception {
        String adminToken = createUserAndGetToken("adminfac2@example.com", Role.ADMIN);

        mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplikat Fac"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplikat Fac"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void update_renamesFacility() throws Exception {
        String adminToken = createUserAndGetToken("adminfac3@example.com", Role.ADMIN);

        String createResponse = mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Stara Nazwa"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long facilityId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/facilities/" + facilityId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nowa Nazwa"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowa Nazwa"));
    }

    @Test
    void delete_unknownFacility_isNotFound() throws Exception {
        String adminToken = createUserAndGetToken("adminfac4@example.com", Role.ADMIN);

        mockMvc.perform(delete("/api/facilities/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_facilityInUse_isConflict() throws Exception {
        String adminToken = createUserAndGetToken("adminfac5@example.com", Role.ADMIN);

        String createResponse = mockMvc.perform(post("/api/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Zajety Zaklad"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long facilityId = objectMapper.readTree(createResponse).get("id").asLong();

        User assigned = User.reconstitute(null, "assigned-fac5@example.com", "hash", "Jan", "Kowalski",
                Role.EMPLOYEE, null, null, "Zajety Zaklad", null, Instant.now());
        userRepository.save(assigned);

        mockMvc.perform(delete("/api/facilities/" + facilityId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }
}
