package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for ReviewRepository using @DataJpaTest (H2 in-memory DB).
 * Tests the custom query methods findBySender, findByReceiver, and
 * existsBySenderAndInserat.
 */
@DataJpaTest
public class ReviewRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Helper: creates and persists a User with the given username and email.
     * All non-nullable columns must be set for the entity to be valid.
     */
    private User persistUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("testPassword");
        user.setSurname("John");
        user.setLastname("Doe");
        user.setEmailAddress(email);
        user.setIsVolunteer(true);
        user.setBio("A bio");
        user.setAddress("Some Street 1");
        user.setGender("male");
        user.setPhoneNumber("+41 79 000 00 00");
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));
        user.setToken(username + "-token");

        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    /**
     * Helper: creates and persists a minimal Inserat.
     * NOTE: extend this if your Inserat entity has additional non-nullable
     * columns (e.g. title, description, creator).
     */
    private Inserat persistInserat(User recipient) {
        Inserat inserat = new Inserat();
        inserat.setRecipient(recipient);
        inserat.setDescription("Test description");
        inserat.setLocation("Test location");
        inserat.setLatitude(47.3769);
        inserat.setLongitude(8.5417);
        inserat.setDate(LocalDate.now().plusDays(1));
        inserat.setTime("12:00");
        inserat.setTimeframe("morning");
        inserat.setWorkType("gardening");

        entityManager.persist(inserat);
        entityManager.flush();
        return inserat;
    }

    /**
     * Helper: creates and persists a Review linking sender, receiver, and inserat.
     */
    private Review persistReview(User sender, User receiver, Inserat inserat) {
        Review review = new Review();
        review.setSender(sender);
        review.setReceiver(receiver);
        review.setInserat(inserat);
        review.setText("");
        review.setReviewStatus(ReviewStatus.PENDING);
        review.setFinished(false);
        review.setCreationDate(LocalDate.now());

        entityManager.persist(review);
        entityManager.flush();
        return review;
    }

    // --- findBySender ------------------------------------------------------

    @Test
    public void findBySender_success() {
        // given: a sender with one review
        User sender = persistUser("alice", "alice@example.com");
        User receiver = persistUser("bob", "bob@example.com");
        Inserat inserat = persistInserat(receiver);
        Review review = persistReview(sender, receiver, inserat);

        // when: searching by sender
        List<Review> found = reviewRepository.findBySender(sender);

        // then: the sender's review is returned
        assertEquals(1, found.size());
        assertEquals(review.getId(), found.get(0).getId());
        assertEquals(sender.getId(), found.get(0).getSender().getId());
        assertEquals(ReviewStatus.PENDING, found.get(0).getReviewStatus());
    }

    @Test
    public void findBySender_noReviews_returnsEmptyList() {
        // given: a sender with no reviews
        User sender = persistUser("alice", "alice@example.com");

        // when: searching by sender
        List<Review> found = reviewRepository.findBySender(sender);

        // then: an empty list is returned (never null)
        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    public void findBySender_onlyReturnsOwnReviews() {
        // given: two users each having sent one review
        User alice = persistUser("alice", "alice@example.com");
        User bob = persistUser("bob", "bob@example.com");
        User carol = persistUser("carol", "carol@example.com");
        Inserat inseratA = persistInserat(bob);
        Inserat inseratB = persistInserat(carol);

        persistReview(alice, bob, inseratA);   // alice → bob
        persistReview(bob, carol, inseratB);   // bob → carol

        // when: searching by alice
        List<Review> aliceReviews = reviewRepository.findBySender(alice);

        // then: only alice's review is returned, not bob's
        assertEquals(1, aliceReviews.size());
        assertEquals(alice.getId(), aliceReviews.get(0).getSender().getId());
    }

    @Test
    public void findBySender_multipleReviews_returnsAll() {
        // given: a sender with two reviews
        User sender = persistUser("alice", "alice@example.com");
        User receiver = persistUser("bob", "bob@example.com");
        Inserat inserat1 = persistInserat(receiver);
        Inserat inserat2 = persistInserat(receiver);

        persistReview(sender, receiver, inserat1);
        persistReview(sender, receiver, inserat2);

        // when: searching by sender
        List<Review> found = reviewRepository.findBySender(sender);

        // then: both reviews are returned
        assertEquals(2, found.size());
    }

    // --- findByReceiver ----------------------------------------------------

    @Test
    public void findByReceiver_success() {
        // given: a receiver with one review
        User sender = persistUser("alice", "alice@example.com");
        User receiver = persistUser("bob", "bob@example.com");
        Inserat inserat = persistInserat(receiver);
        Review review = persistReview(sender, receiver, inserat);

        // when: searching by receiver
        List<Review> found = reviewRepository.findByReceiver(receiver);

        // then: the receiver's review is returned
        assertEquals(1, found.size());
        assertEquals(review.getId(), found.get(0).getId());
        assertEquals(receiver.getId(), found.get(0).getReceiver().getId());
    }

    @Test
    public void findByReceiver_noReviews_returnsEmptyList() {
        // given: a receiver with no reviews
        User receiver = persistUser("bob", "bob@example.com");

        // when: searching by receiver
        List<Review> found = reviewRepository.findByReceiver(receiver);

        // then: an empty list is returned
        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    public void findByReceiver_onlyReturnsOwnReviews() {
        // given: bob receives one review, carol receives another
        User alice = persistUser("alice", "alice@example.com");
        User bob = persistUser("bob", "bob@example.com");
        User carol = persistUser("carol", "carol@example.com");
        Inserat inseratA = persistInserat(bob);
        Inserat inseratB = persistInserat(carol);

        persistReview(alice, bob, inseratA);     // alice → bob
        persistReview(alice, carol, inseratB);   // alice → carol

        // when: searching by bob
        List<Review> bobReviews = reviewRepository.findByReceiver(bob);

        // then: only bob's received review is returned
        assertEquals(1, bobReviews.size());
        assertEquals(bob.getId(), bobReviews.get(0).getReceiver().getId());
    }

    // --- existsBySenderAndInserat ------------------------------------------

    @Test
    public void existsBySenderAndInserat_existing_returnsTrue() {
        // given: a review exists for sender + inserat
        User sender = persistUser("alice", "alice@example.com");
        User receiver = persistUser("bob", "bob@example.com");
        Inserat inserat = persistInserat(receiver);
        persistReview(sender, receiver, inserat);

        // when: checking existence
        boolean exists = reviewRepository.existsBySenderAndInserat(sender, inserat);

        // then: true is returned
        assertTrue(exists);
    }

    @Test
    public void existsBySenderAndInserat_noReview_returnsFalse() {
        // given: a sender and inserat exist but no review links them
        User sender = persistUser("alice", "alice@example.com");
        User receiver = persistUser("bob", "bob@example.com");
        Inserat inserat = persistInserat(receiver);

        // when: checking existence
        boolean exists = reviewRepository.existsBySenderAndInserat(sender, inserat);

        // then: false is returned
        assertFalse(exists);
    }

    @Test
    public void existsBySenderAndInserat_differentSender_returnsFalse() {
        // given: a review exists for alice + inserat, but carol checks for the same inserat
        User alice = persistUser("alice", "alice@example.com");
        User bob = persistUser("bob", "bob@example.com");
        User carol = persistUser("carol", "carol@example.com");
        Inserat inserat = persistInserat(bob);
        persistReview(alice, bob, inserat);

        // when: checking with a different sender
        boolean exists = reviewRepository.existsBySenderAndInserat(carol, inserat);

        // then: false is returned (the review belongs to alice, not carol)
        assertFalse(exists);
    }

    @Test
    public void existsBySenderAndInserat_differentInserat_returnsFalse() {
        // given: a review exists for alice + inserat1, but we check inserat2
        User alice = persistUser("alice", "alice@example.com");
        User bob = persistUser("bob", "bob@example.com");
        Inserat inserat1 = persistInserat(bob);
        Inserat inserat2 = persistInserat(bob);
        persistReview(alice, bob, inserat1);

        // when: checking with a different inserat
        boolean exists = reviewRepository.existsBySenderAndInserat(alice, inserat2);

        // then: false is returned
        assertFalse(exists);
    }
}