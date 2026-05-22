package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InseratRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test covering the help-request happy-path workflow against a
 * real H2 in-memory database. This addresses the M3 TA feedback that asked
 * for tests on the main application logic (request creation + volunteer
 * acceptance) rather than only user creation.
 *
 * What is exercised end-to-end (no mocks):
 *  1. A recipient creates a help request via InseratService.createInserat.
 *  2. A volunteer applies via InseratService.applyToInserat.
 *  3. The recipient accepts that volunteer via InseratService.acceptVolunteer.
 *     This must also trigger ReviewService.acceptedInseratCreateReview for
 *     both parties, so we verify the two PENDING reviews land in the DB.
 *  4. The volunteer attempts to apply again -> rejected (already applied).
 *  5. A second volunteer tries to be accepted -> rejected (slot taken).
 *
 * The flow tests business-rule cooperation between InseratService, UserService,
 * ReviewService and the JPA repositories.
 */
@WebAppConfiguration
@SpringBootTest
public class InseratServiceIntegrationTest {

    @Qualifier("inseratRepository")
    @Autowired
    private InseratRepository inseratRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Qualifier("reviewRepository")
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private InseratService inseratService;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        // Order matters: reviews and inserats reference users.
        reviewRepository.deleteAll();
        inseratRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String username, String email, boolean isVolunteer) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("testPassword");
        u.setSurname("Given");
        u.setLastname("Family");
        u.setEmailAddress(email);
        u.setIsVolunteer(isVolunteer);
        u.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return userService.createUser(u);
    }

    private Inserat buildInserat() {
        Inserat i = new Inserat();
        i.setDescription("Help me move a couch.");
        i.setLocation("Bahnhofstrasse 1, 8001 Zurich");
        i.setLatitude(47.3769);
        i.setLongitude(8.5417);
        i.setDate(LocalDate.now().plusDays(2));
        i.setTimeframe("2");
        i.setWorkType("HEAVY_LIFTING");
        return i;
    }

    @Test
    public void fullWorkflow_recipientCreatesAcceptsVolunteer_persistsReviews() {
        // given: a recipient and a volunteer exist in the DB
        User recipient = createUser("rosa", "rosa@example.com", false);
        User volunteer = createUser("victor", "victor@example.com", true);

        // when: the recipient posts a help request
        Inserat created = inseratService.createInserat(buildInserat(), recipient.getId());
        assertNotNull(created.getId());
        assertEquals(InseratStatus.OPEN, created.getStatus());
        assertEquals(recipient.getId(), created.getRecipient().getId());

        // when: the volunteer applies
        Inserat afterApply = inseratService.applyToInserat(created.getId(), volunteer.getId());
        assertEquals(1, afterApply.getVolunteerApplied().size());
        assertEquals(volunteer.getId(), afterApply.getVolunteerApplied().get(0).getId());

        // when: the recipient accepts the volunteer
        Inserat afterAccept = inseratService.acceptVolunteer(created.getId(), volunteer.getId());
        assertEquals(InseratStatus.ACCEPTED, afterAccept.getStatus());
        assertEquals(volunteer.getId(), afterAccept.getVolunteerAccepted().getId());

        // then: acceptedInseratCreateReview should have produced 2 reviews
        // (one for each direction). Persisted in the DB.
        List<ch.uzh.ifi.hase.soprafs26.entity.Review> reviews = reviewRepository.findAll();
        assertEquals(2, reviews.size(), "Two pending reviews should be created on acceptance");
        long volunteerToRecipient = reviews.stream()
            .filter(r -> r.getSender().getId().equals(volunteer.getId())
                      && r.getReceiver().getId().equals(recipient.getId()))
            .count();
        long recipientToVolunteer = reviews.stream()
            .filter(r -> r.getSender().getId().equals(recipient.getId())
                      && r.getReceiver().getId().equals(volunteer.getId()))
            .count();
        assertEquals(1, volunteerToRecipient);
        assertEquals(1, recipientToVolunteer);
    }

    @Test
    public void applyToInserat_volunteerAppliesTwice_throwsBadRequest() {
        User recipient = createUser("recipient", "r@example.com", false);
        User volunteer = createUser("volunteer", "v@example.com", true);
        Inserat created = inseratService.createInserat(buildInserat(), recipient.getId());

        inseratService.applyToInserat(created.getId(), volunteer.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat(created.getId(), volunteer.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void applyToInserat_ownInserat_throwsBadRequest() {
        // The recipient must not be able to apply to their own help request.
        // (Volunteer flag is set so the volunteer guard passes.)
        User self = createUser("self", "self@example.com", true);
        Inserat created = inseratService.createInserat(buildInserat(), self.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> inseratService.applyToInserat(created.getId(), self.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void acceptVolunteer_secondAttempt_throwsConflict() {
        User recipient = createUser("recipient", "r@example.com", false);
        User volunteerA = createUser("volA", "a@example.com", true);
        User volunteerB = createUser("volB", "b@example.com", true);
        Inserat created = inseratService.createInserat(buildInserat(), recipient.getId());

        inseratService.applyToInserat(created.getId(), volunteerA.getId());
        inseratService.applyToInserat(created.getId(), volunteerB.getId());

        inseratService.acceptVolunteer(created.getId(), volunteerA.getId());

        // The slot is now taken: even an accepted-but-different-volunteer call
        // must fail with 409 Conflict.
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> inseratService.acceptVolunteer(created.getId(), volunteerB.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void acceptVolunteer_volunteerNeverApplied_throwsBadRequest() {
        User recipient = createUser("recipient", "r@example.com", false);
        User volunteer = createUser("volunteer", "v@example.com", true);
        Inserat created = inseratService.createInserat(buildInserat(), recipient.getId());

        // Volunteer was never added to the applied list.
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> inseratService.acceptVolunteer(created.getId(), volunteer.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void unapplyFromInserat_volunteerWithdraws_removesFromAppliedList() {
        User recipient = createUser("recipient", "r@example.com", false);
        User volunteer = createUser("volunteer", "v@example.com", true);
        Inserat created = inseratService.createInserat(buildInserat(), recipient.getId());

        Inserat afterApply = inseratService.applyToInserat(created.getId(), volunteer.getId());
        assertEquals(1, afterApply.getVolunteerApplied().size());

        Inserat afterUnapply = inseratService.unapplyFromInserat(created.getId(), volunteer.getId());
        assertEquals(0, afterUnapply.getVolunteerApplied().size());
    }
}
