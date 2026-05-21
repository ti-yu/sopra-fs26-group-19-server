package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReviewService, focused on:
 *  - star rating validation in userWritesReview (#132),
 *  - dismissReviewForNow + reviewPopupNecessary cool-down (#151).
 *
 * The pre-existing time-based status logic in updateReviewStatusBasedOnObjective
 * is not retested here; only the new code paths are covered.
 */
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review pendingReview;
    private User sender;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        sender = new User();
        sender.setId("u1");
        sender.setUsername("alice");

        pendingReview = new Review();
        pendingReview.setId("r1");
        pendingReview.setSender(sender);
        pendingReview.setReviewStatus(ReviewStatus.PENDING);
        pendingReview.setFinished(false);
        pendingReview.setText("");

        // repo.save returns its argument unchanged so we can assert on the modified entity
        Mockito.when(reviewRepository.save(Mockito.any(Review.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // --- userWritesReview / star validation --------------------------------

    @Test
    public void userWritesReview_validStars_success() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        Review result = reviewService.userWritesReview("r1", "Great help, thank you!", 4.5);

        assertEquals(4.5, result.getStars());
        assertEquals("Great help, thank you!", result.getText());
        assertEquals(ReviewStatus.WRITTEN, result.getReviewStatus());
        assertTrue(result.isFinished());
        assertNull(result.getIgnoreUntil()); // cleared on write
    }

    @Test
    public void userWritesReview_nullStars_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "Good", null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_starsTooLow_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "Good", 0.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_starsTooHigh_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "Good", 5.5));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_starsNotHalfStep_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "Good", 3.3));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_emptyText_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "   ", 4.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_ignored_throwsBadRequest() {
        pendingReview.setReviewStatus(ReviewStatus.IGNORED);
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", "Good", 4.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_notFound_throws404() {
        Mockito.when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("missing", "Good", 4.0));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- dismissReviewForNow -----------------------------------------------

    @Test
    public void dismissReviewForNow_setsIgnoreUntil24hAhead() {
        // Mock the findById call to return your dummy pendingReview object
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        java.time.LocalDateTime expectedTime = java.time.LocalDateTime.now(java.time.ZoneId.of("Europe/Zurich")).plusHours(24);

        Review updatedReview = reviewService.dismissReviewForNow("r1");

        org.assertj.core.api.Assertions.assertThat(updatedReview.getIgnoreUntil())
                .isCloseTo(expectedTime, org.assertj.core.api.Assertions.within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    public void dismissReviewForNow_finishedReview_throwsBadRequest() {
        pendingReview.setFinished(true);
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.dismissReviewForNow("r1"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void dismissReviewForNow_notFound_throws404() {
        Mockito.when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.dismissReviewForNow("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- reviewPopupNecessary respects ignoreUntil --------------------------

    @Test
    public void reviewPopupNecessary_returnsPending_whenNotDismissed() {
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(pendingReview));

        Review result = reviewService.reviewPopupNecessary(sender);
        assertNotNull(result);
        assertEquals("r1", result.getId());
    }

    @Test
    public void reviewPopupNecessary_skipsReview_whileIgnoreUntilInFuture() {
        // Future timestamp: popup should suppress this review.
        pendingReview.setIgnoreUntil(LocalDateTime.now().plusHours(5));
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(pendingReview));

        Review result = reviewService.reviewPopupNecessary(sender);
        assertNull(result);
    }

    @Test
    public void reviewPopupNecessary_returnsReview_whenIgnoreUntilExpired() {
        // Past timestamp: popup should re-appear.
        pendingReview.setIgnoreUntil(LocalDateTime.now().minusHours(1));
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(pendingReview));

        Review result = reviewService.reviewPopupNecessary(sender);
        assertNotNull(result);
        assertEquals("r1", result.getId());
    }

    @Test
    public void reviewPopupNecessary_skipsFinishedReviews() {
        pendingReview.setFinished(true);
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(pendingReview));

        assertNull(reviewService.reviewPopupNecessary(sender));
    }

    @Test
    public void reviewPopupNecessary_skipsIgnoredStatus() {
        pendingReview.setReviewStatus(ReviewStatus.IGNORED);
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(pendingReview));

        assertNull(reviewService.reviewPopupNecessary(sender));
    }

    @Test
    public void reviewPopupNecessary_empty_returnsNull() {
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(Collections.emptyList());

        assertNull(reviewService.reviewPopupNecessary(sender));
    }
}
