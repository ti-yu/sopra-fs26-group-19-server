package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.InseratService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * InseratControllerTest
 * Tests help-request REST endpoints (create, list, apply, accept, dismiss,
 * edit, applicants, map view) with MockMvc.
 */
@WebMvcTest(InseratController.class)
public class InseratControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InseratService inseratService;

    private User createRecipient() {
        User u = new User();
        u.setId("recipient-1");
        u.setUsername("alice");
        u.setSurname("Alice");
        u.setLastname("Muster");
        u.setIsVolunteer(false);
        u.setDateOfBirth(LocalDate.of(1990, 5, 1));
        u.setPhoneNumber("+41 79 000 00 01");
        u.setEmailAddress("alice@example.com");
        return u;
    }

    private User createVolunteer() {
        User u = new User();
        u.setId("volunteer-1");
        u.setUsername("bob");
        u.setIsVolunteer(true);
        return u;
    }

    private Inserat createInserat() {
        Inserat i = new Inserat();
        i.setId("inserat-1");
        i.setRecipient(createRecipient());
        i.setDescription("Help needed");
        i.setLocation("Zürich");
        i.setLatitude(47.3769);
        i.setLongitude(8.5417);
        i.setDate(LocalDate.now().plusDays(3));
        i.setTimeframe("2");
        i.setWorkType("GARDENING");
        i.setStatus(InseratStatus.OPEN);
        i.setVolunteerApplied(new ArrayList<>());
        return i;
    }

    @Test
    public void createInserat_returns201() throws Exception {
        Inserat created = createInserat();
        given(inseratService.createInserat(Mockito.any(), Mockito.eq("recipient-1")))
            .willReturn(created);

        InseratPostDTO dto = new InseratPostDTO();
        dto.setRecipientId("recipient-1");
        dto.setDescription("Help needed");
        dto.setLocation("Zürich");
        dto.setLatitude(47.3769);
        dto.setLongitude(8.5417);
        dto.setDate(LocalDate.now().plusDays(3));
        dto.setTimeframe("2");
        dto.setWorkType("GARDENING");

        mockMvc.perform(post("/help-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is("inserat-1")))
            .andExpect(jsonPath("$.description", is("Help needed")));
    }

    @Test
    public void getInseratsByRecipientId_returnsList() throws Exception {
        given(inseratService.getInseratsByRecipientId("recipient-1"))
            .willReturn(List.of(createInserat()));

        mockMvc.perform(get("/users/recipient-1/help-requests"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is("inserat-1")));
    }

    @Test
    public void getApplicationsByVolunteerId_returnsList() throws Exception {
        given(inseratService.getApplicationsByVolunteerId("volunteer-1"))
            .willReturn(List.of(createInserat()));

        mockMvc.perform(get("/users/volunteer-1/applications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].description", is("Help needed")));
    }

    @Test
    public void applyToInserat_returns200() throws Exception {
        Inserat withApplicant = createInserat();
        withApplicant.setVolunteerApplied(new ArrayList<>(Arrays.asList(createVolunteer())));
        given(inseratService.applyToInserat("inserat-1", "volunteer-1"))
            .willReturn(withApplicant);

        mockMvc.perform(post("/help-requests/inserat-1/apply/volunteer-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("inserat-1")));
    }

    @Test
    public void applyToInserat_conflict_returns400() throws Exception {
        given(inseratService.applyToInserat("inserat-1", "volunteer-1"))
            .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "already applied"));

        mockMvc.perform(post("/help-requests/inserat-1/apply/volunteer-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void acceptVolunteer_returns200() throws Exception {
        Inserat accepted = createInserat();
        accepted.setStatus(InseratStatus.ACCEPTED);
        accepted.setVolunteerAccepted(createVolunteer());
        given(inseratService.acceptVolunteer("inserat-1", "volunteer-1")).willReturn(accepted);

        mockMvc.perform(put("/help-requests/inserat-1/accept/volunteer-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    public void dismissVolunteer_returns200() throws Exception {
        given(inseratService.dismissVolunteer("inserat-1", "volunteer-1"))
            .willReturn(createInserat());

        mockMvc.perform(put("/help-requests/inserat-1/dismiss/volunteer-1"))
            .andExpect(status().isOk());
    }

    @Test
    public void getApplicants_returnsList() throws Exception {
        given(inseratService.getApplicants("inserat-1"))
            .willReturn(List.of(createVolunteer()));

        mockMvc.perform(get("/help-requests/inserat-1/applicants"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is("volunteer-1")));
    }

    @Test
    public void getInseratById_returns200() throws Exception {
        given(inseratService.getInseratById("inserat-1")).willReturn(createInserat());

        mockMvc.perform(get("/help-requests/inserat-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("inserat-1")));
    }

    @Test
    public void getInseratById_notFound_returns404() throws Exception {
        given(inseratService.getInseratById("ghost"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));

        mockMvc.perform(get("/help-requests/ghost"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void editInserat_returns200() throws Exception {
        Inserat edited = createInserat();
        edited.setDescription("Updated description");
        given(inseratService.editInserat(Mockito.eq("inserat-1"), Mockito.any()))
            .willReturn(edited);

        InseratPostDTO dto = new InseratPostDTO();
        dto.setDescription("Updated description");

        mockMvc.perform(put("/help-requests/inserat-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    public void getAllInserats_returnsMapList() throws Exception {
        given(inseratService.getAllInserats()).willReturn(List.of(createInserat()));

        mockMvc.perform(get("/help-requests-map"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].latitude", is(47.3769)));
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Request body serialization failed: %s", e));
        }
    }
}
