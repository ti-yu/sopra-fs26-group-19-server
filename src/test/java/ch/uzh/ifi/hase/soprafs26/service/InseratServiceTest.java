package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InseratRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InseratService. Mocks InseratRepository and UserRepository
 * to isolate service logic (apply / accept / dismiss / edit / auto-finish).
 */
public class InseratServiceTest {

    @Mock
    private InseratRepository inseratRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InseratService inseratService;

    private User recipient;
    private User volunteer;
    private Inserat inserat;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        recipient = new User();
        recipient.setId("recipient-1");
        recipient.setUsername("alice");
        recipient.setIsVolunteer(false);

        volunteer = new User();
        volunteer.setId("volunteer-1");
        volunteer.setUsername("bob");
        volunteer.setIsVolunteer(true);

        inserat = new Inserat();
        inserat.setId("inserat-1");
        inserat.setRecipient(recipient);
        inserat.setDescription("Need help moving");
        inserat.setLocation("Zürich");
        inserat.setLatitude(47.3769);
        inserat.setLongitude(8.5417);
        inserat.setDate(LocalDate.now().plusDays(5));
        inserat.setTimeframe("2");
        inserat.setWorkType("HEAVY_LIFTING");
        inserat.setStatus(InseratStatus.OPEN);
        inserat.setVolunteerApplied(new ArrayList<>());
    }

    // ── createInserat ───────────────────────────────────────────────

    @Test
    public void createInserat_validData_success() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        Mockito.when(inseratRepository.save(Mockito.any())).thenReturn(inserat);

        Inserat created = inseratService.createInserat(inserat, "recipient-1");

        assertEquals("Need help moving", created.getDescription());
        assertEquals(recipient, created.getRecipient());
        Mockito.verify(inseratRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void createInserat_missingDescription_throws400() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        inserat.setDescription("  ");
        assertThrows(ResponseStatusException.class,
            () -> inseratService.createInserat(inserat, "recipient-1"));
    }

    @Test
    public void createInserat_missingDate_throws400() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        inserat.setDate(null);
        assertThrows(ResponseStatusException.class,
            () -> inseratService.createInserat(inserat, "recipient-1"));
    }

    @Test
    public void createInserat_missingLocation_throws400() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        inserat.setLocation("");
        assertThrows(ResponseStatusException.class,
            () -> inseratService.createInserat(inserat, "recipient-1"));
    }

    @Test
    public void createInserat_missingTimeframe_throws400() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        inserat.setTimeframe(null);
        assertThrows(ResponseStatusException.class,
            () -> inseratService.createInserat(inserat, "recipient-1"));
    }

    @Test
    public void createInserat_userNotFound_throws404() {
        Mockito.when(userRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
            () -> inseratService.createInserat(inserat, "ghost"));
    }

    // ── getInseratsByRecipientId / getApplicationsByVolunteerId ────

    @Test
    public void getInseratsByRecipientId_returnsList() {
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        Mockito.when(inseratRepository.findByRecipient(recipient)).thenReturn(List.of(inserat));

        List<Inserat> result = inseratService.getInseratsByRecipientId("recipient-1");

        assertEquals(1, result.size());
        assertEquals("inserat-1", result.get(0).getId());
    }

    @Test
    public void getApplicationsByVolunteerId_returnsList() {
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));
        Mockito.when(inseratRepository.findByVolunteerAppliedContaining(volunteer))
            .thenReturn(List.of(inserat));

        List<Inserat> result = inseratService.getApplicationsByVolunteerId("volunteer-1");

        assertEquals(1, result.size());
    }

    @Test
    public void getInseratsByRecipientId_autoFinishesPastOpenInserats() {
        inserat.setDate(LocalDate.now().minusDays(1));
        inserat.setStatus(InseratStatus.OPEN);
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        Mockito.when(inseratRepository.findByRecipient(recipient)).thenReturn(List.of(inserat));

        List<Inserat> result = inseratService.getInseratsByRecipientId("recipient-1");

        assertEquals(InseratStatus.DONE, result.get(0).getStatus());
    }

    // ── applyToInserat ─────────────────────────────────────────────

    @Test
    public void applyToInserat_valid_success() {
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        Inserat result = inseratService.applyToInserat("inserat-1", "volunteer-1");

        assertTrue(result.getVolunteerApplied().contains(volunteer));
    }

    @Test
    public void applyToInserat_selfApply_throws400() {
        User self = new User();
        self.setId("recipient-1");
        self.setIsVolunteer(true);
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("recipient-1")).thenReturn(Optional.of(self));

        assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat("inserat-1", "recipient-1"));
    }

    @Test
    public void applyToInserat_nonVolunteer_throws400() {
        User recipientTryingToApply = new User();
        recipientTryingToApply.setId("v2");
        recipientTryingToApply.setIsVolunteer(false);
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("v2")).thenReturn(Optional.of(recipientTryingToApply));

        assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat("inserat-1", "v2"));
    }

    @Test
    public void applyToInserat_notOpen_throws400() {
        inserat.setStatus(InseratStatus.ACCEPTED);
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat("inserat-1", "volunteer-1"));
    }

    @Test
    public void applyToInserat_alreadyApplied_throws400() {
        inserat.setVolunteerApplied(new ArrayList<>(Arrays.asList(volunteer)));
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat("inserat-1", "volunteer-1"));
    }

    @Test
    public void applyToInserat_inseratNotFound_throws404() {
        Mockito.when(inseratRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat("ghost", "volunteer-1"));
    }

    // ── acceptVolunteer / dismissVolunteer ─────────────────────────

    @Test
    public void acceptVolunteer_valid_success() {
        inserat.setVolunteerApplied(new ArrayList<>(Arrays.asList(volunteer)));
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        Inserat result = inseratService.acceptVolunteer("inserat-1", "volunteer-1");

        assertEquals(volunteer, result.getVolunteerAccepted());
        assertEquals(InseratStatus.ACCEPTED, result.getStatus());
    }

    @Test
    public void acceptVolunteer_notApplied_throws400() {
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        assertThrows(ResponseStatusException.class,
            () -> inseratService.acceptVolunteer("inserat-1", "volunteer-1"));
    }

    @Test
    public void dismissVolunteer_valid_removesFromList() {
        inserat.setVolunteerApplied(new ArrayList<>(Arrays.asList(volunteer)));
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Mockito.when(userRepository.findById("volunteer-1")).thenReturn(Optional.of(volunteer));

        Inserat result = inseratService.dismissVolunteer("inserat-1", "volunteer-1");

        assertFalse(result.getVolunteerApplied().contains(volunteer));
    }

    // ── editInserat / getInseratById / getApplicants ───────────────

    @Test
    public void editInserat_updatesProvidedFields() {
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));

        Inserat update = new Inserat();
        update.setDescription("Updated description");
        update.setLocation("Basel");
        update.setDate(LocalDate.now().plusDays(10));
        update.setTimeframe("3");

        Inserat result = inseratService.editInserat("inserat-1", update);

        assertEquals("Updated description", result.getDescription());
        assertEquals("Basel", result.getLocation());
        assertEquals("3", result.getTimeframe());
    }

    @Test
    public void editInserat_inseratNotFound_throws404() {
        Mockito.when(inseratRepository.findById("ghost")).thenReturn(Optional.empty());
        Inserat update = new Inserat();
        update.setDescription("anything");
        assertThrows(ResponseStatusException.class,
            () -> inseratService.editInserat("ghost", update));
    }

    @Test
    public void getInseratById_found_returns() {
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        Inserat result = inseratService.getInseratById("inserat-1");
        assertEquals("inserat-1", result.getId());
    }

    @Test
    public void getInseratById_notFound_throws404() {
        Mockito.when(inseratRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
            () -> inseratService.getInseratById("ghost"));
    }

    @Test
    public void getApplicants_returnsVolunteerList() {
        inserat.setVolunteerApplied(new ArrayList<>(Arrays.asList(volunteer)));
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        List<User> applicants = inseratService.getApplicants("inserat-1");
        assertEquals(1, applicants.size());
        assertEquals("volunteer-1", applicants.get(0).getId());
    }

    @Test
    public void getRecipientByInserat_returnsRecipient() {
        Mockito.when(inseratRepository.findById("inserat-1")).thenReturn(Optional.of(inserat));
        User result = inseratService.getRecipientByInserat("inserat-1");
        assertEquals("recipient-1", result.getId());
    }

    @Test
    public void getAllInserats_filtersOutNullCoordinates() {
        Inserat noCoords = new Inserat();
        noCoords.setId("no-coords");
        noCoords.setLatitude(null);
        noCoords.setLongitude(null);
        Mockito.when(inseratRepository.findAll()).thenReturn(Arrays.asList(inserat, noCoords));

        List<Inserat> result = inseratService.getAllInserats();
        assertEquals(1, result.size());
        assertEquals("inserat-1", result.get(0).getId());
    }
}
