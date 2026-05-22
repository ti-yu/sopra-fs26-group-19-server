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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.uzh.ifi.hase.soprafs26.entity.Inserat;

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

        Review  result = reviewService.reviewPopupNecessary(sender);
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

    private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");

    // --- updateReviewStatusBasedOnObjective: status transitions --------------

    @Test
    public void updateStatus_finishedReview_noChange() {
        pendingReview.setFinished(true);
        pendingReview.setReviewStatus(ReviewStatus.WRITTEN);

        ReviewStatus result = reviewService.updateReviewStatusBasedOnObjective(pendingReview);

        assertEquals(ReviewStatus.WRITTEN, result);
        Mockito.verify(reviewRepository, Mockito.never()).save(pendingReview);
    }

    @Test
    public void updateStatus_ignoredStatus_setsFinished() {
        pendingReview.setReviewStatus(ReviewStatus.IGNORED);

        ReviewStatus result = reviewService.updateReviewStatusBasedOnObjective(pendingReview);

        assertEquals(ReviewStatus.IGNORED, result);
        assertTrue(pendingReview.isFinished());
    }

    @Test
    public void updateStatus_inseratInFuture_setsHasNotHappened() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).plusDays(5));
        pendingReview.setInserat(inserat);

        assertEquals(ReviewStatus.HASNOTHAPPENED,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
    }

    @Test
    public void updateStatus_inseratTodayLaterTime_setsHasNotHappened() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH));
        inserat.setTime("23:59"); // safely later than "now" except in last minute of day
        pendingReview.setInserat(inserat);

        assertEquals(ReviewStatus.HASNOTHAPPENED,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
    }

    @Test
    public void updateStatus_inseratTodayPastTime_setsPending() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH));
        inserat.setTime("00:00");
        pendingReview.setInserat(inserat);

        assertEquals(ReviewStatus.PENDING,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
    }

    @Test
    public void updateStatus_inseratRecentlyPast_setsPending() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).minusDays(10));
        pendingReview.setInserat(inserat);

        assertEquals(ReviewStatus.PENDING,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
        assertFalse(pendingReview.isFinished());
    }

    @Test
    public void updateStatus_inseratOver3MonthsPast_setsIgnoredAndFinished() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).minusMonths(4));
        pendingReview.setInserat(inserat);

        assertEquals(ReviewStatus.IGNORED,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
        assertTrue(pendingReview.isFinished());
    }

    @Test
    public void updateStatus_textAlreadyPresent_setsWritten() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).minusDays(5));
        pendingReview.setInserat(inserat);
        pendingReview.setText("Was already filled in");

        assertEquals(ReviewStatus.WRITTEN,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
        assertTrue(pendingReview.isFinished());
    }

    // --- acceptedInseratCreateReview: link between inserat and review --------

    @Test
    public void acceptedInseratCreateReview_newReview_createsWithCorrectFields() {
        User receiver = new User();
        receiver.setId("u2");
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).plusDays(3)); // future → HASNOTHAPPENED

        Mockito.when(reviewRepository.existsBySenderAndInserat(sender, inserat))
                .thenReturn(false);

        Review result = reviewService.acceptedInseratCreateReview(sender, receiver, inserat);

        assertNotNull(result);
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals(inserat, result.getInserat());
        assertEquals("", result.getText());
        assertFalse(result.isFinished());
        assertEquals(ReviewStatus.HASNOTHAPPENED, result.getReviewStatus());
        assertEquals(LocalDate.now(ZURICH), result.getCreationDate());
    }

    @Test
    public void acceptedInseratCreateReview_pastInserat_immediatelyPending() {
        User receiver = new User();
        receiver.setId("u2");
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH).minusDays(2));

        Mockito.when(reviewRepository.existsBySenderAndInserat(sender, inserat))
                .thenReturn(false);

        Review result = reviewService.acceptedInseratCreateReview(sender, receiver, inserat);

        assertEquals(ReviewStatus.PENDING, result.getReviewStatus());
    }

    @Test
    public void acceptedInseratCreateReview_duplicate_returnsNullAndDoesNotSave() {
        User receiver = new User();
        receiver.setId("u2");
        Inserat inserat = new Inserat();

        Mockito.when(reviewRepository.existsBySenderAndInserat(sender, inserat))
                .thenReturn(true);

        Review result = reviewService.acceptedInseratCreateReview(sender, receiver, inserat);

        assertNull(result);
        Mockito.verify(reviewRepository, Mockito.never()).save(Mockito.any(Review.class));
    }

    // --- userIgnoresReview ---------------------------------------------------

    @Test
    public void userIgnoresReview_marksIgnoredAndFinished() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));

        Review result = reviewService.userIgnoresReview("r1");

        assertEquals(ReviewStatus.IGNORED, result.getReviewStatus());
        assertTrue(result.isFinished());
    }

    @Test
    public void userIgnoresReview_notFound_throws404() {
        Mockito.when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userIgnoresReview("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- getReviewsBySenderId / getReviewsByReceiverId -----------------------

    @Test
    public void getReviewsBySenderId_userExists_returnsList() {
        Mockito.when(userRepository.findById("u1")).thenReturn(Optional.of(sender));
        Mockito.when(reviewRepository.findBySender(sender)).thenReturn(List.of(pendingReview));

        List<Review> result = reviewService.getReviewsBySenderId("u1");

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getId());
    }

    @Test
    public void getReviewsBySenderId_userNotFound_throws404() {
        Mockito.when(userRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.getReviewsBySenderId("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void getReviewsByReceiverId_userNotFound_throws404() {
        Mockito.when(userRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.getReviewsByReceiverId("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --- fetchReceivedReviews: only WRITTEN are returned ---------------------

    @Test
    public void fetchReceivedReviews_returnsOnlyWritten() {
        Review written = new Review();
        written.setId("r2");
        written.setReviewStatus(ReviewStatus.WRITTEN);

        Review pending = new Review();
        pending.setId("r3");
        pending.setReviewStatus(ReviewStatus.PENDING);

        Review ignored = new Review();
        ignored.setId("r4");
        ignored.setReviewStatus(ReviewStatus.IGNORED);

        Mockito.when(reviewRepository.findByReceiver(sender))
                .thenReturn(List.of(written, pending, ignored));

        List<Review> result = reviewService.fetchReceivedReviews(sender);

        assertEquals(1, result.size());
        assertEquals("r2", result.get(0).getId());
    }

    // --- userWritesReview: text length boundary ------------------------------

    @Test
    public void userWritesReview_textOver100Chars_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
        String tooLong = "a".repeat(101);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", tooLong, 4.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void userWritesReview_textExactly100Chars_succeeds() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
        String maxText = "a".repeat(100);

        Review result = reviewService.userWritesReview("r1", maxText, 4.0);

        assertEquals(ReviewStatus.WRITTEN, result.getReviewStatus());
        assertEquals(100, result.getText().length());
    }



    @Test
    public void fetchDoneReviews_returnsOnlyFinishedReviews() {
        Review written = new Review();
        written.setId("done-1");
        written.setSender(sender);
        written.setReviewStatus(ReviewStatus.WRITTEN);
        written.setFinished(true);
        written.setText("Already written");
    
        Review unfinished = new Review();
        unfinished.setId("pending-1");
        unfinished.setSender(sender);
        unfinished.setReviewStatus(ReviewStatus.PENDING);
        unfinished.setFinished(false);
        unfinished.setText("");
        Inserat futureInserat = new Inserat();
        futureInserat.setDate(LocalDate.now(ZURICH).plusDays(2));
        unfinished.setInserat(futureInserat);
    
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(written, unfinished));
    
        List<Review> result = reviewService.fetchDoneReviews(sender);
    
        assertEquals(1, result.size());
        assertEquals("done-1", result.get(0).getId());
    }
    
    @Test
    public void fetchDoneReviews_flipsOverdueReviewToFinishedAndIncludesIt() {
        // Inserat over 3 months past -> updateReviewStatusBasedOnObjective
        // marks the review IGNORED + finished, so it should appear in the result.
        Review overdue = new Review();
        overdue.setId("overdue-1");
        overdue.setSender(sender);
        overdue.setReviewStatus(ReviewStatus.PENDING);
        overdue.setFinished(false);
        overdue.setText("");
        Inserat oldInserat = new Inserat();
        oldInserat.setDate(LocalDate.now(ZURICH).minusMonths(4));
        overdue.setInserat(oldInserat);
    
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(overdue));
    
        List<Review> result = reviewService.fetchDoneReviews(sender);
    
        assertEquals(1, result.size());
        assertEquals("overdue-1", result.get(0).getId());
        assertTrue(result.get(0).isFinished());
        assertEquals(ReviewStatus.IGNORED, result.get(0).getReviewStatus());
    }
    
    @Test
    public void fetchDoneReviews_emptyList_returnsEmpty() {
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(Collections.emptyList());
    
        assertTrue(reviewService.fetchDoneReviews(sender).isEmpty());
    }
    
    // --- getReviewsByReceiverId: happy path ----------------------------------
    
    @Test
    public void getReviewsByReceiverId_userExists_returnsList() {
        Mockito.when(userRepository.findById("u1")).thenReturn(Optional.of(sender));
        Mockito.when(reviewRepository.findByReceiver(sender)).thenReturn(List.of(pendingReview));
    
        List<Review> result = reviewService.getReviewsByReceiverId("u1");
    
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getId());
    }
    
    // --- updateReviewStatusBasedOnObjective: today + missing time -----------
    
    @Test
    public void updateStatus_inseratTodayNullTime_setsPending() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH));
        inserat.setTime(null);
        pendingReview.setInserat(inserat);
    
        assertEquals(ReviewStatus.PENDING,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
    }
    
    @Test
    public void updateStatus_inseratTodayBlankTime_setsPending() {
        Inserat inserat = new Inserat();
        inserat.setDate(LocalDate.now(ZURICH));
        inserat.setTime("   ");
        pendingReview.setInserat(inserat);
    
        assertEquals(ReviewStatus.PENDING,
                reviewService.updateReviewStatusBasedOnObjective(pendingReview));
    }
    
    // --- userWritesReview: gaps ---------------------------------------------
    
    @Test
    public void userWritesReview_nullText_throwsBadRequest() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
    
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.userWritesReview("r1", null, 4.0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
    
    @Test
    public void userWritesReview_starsAtLowerBoundary_succeeds() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
    
        Review result = reviewService.userWritesReview("r1", "Bare minimum", 0.5);
    
        assertEquals(0.5, result.getStars());
        assertEquals(ReviewStatus.WRITTEN, result.getReviewStatus());
        assertTrue(result.isFinished());
    }
    
    @Test
    public void userWritesReview_starsAtUpperBoundary_succeeds() {
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
    
        Review result = reviewService.userWritesReview("r1", "Top marks", 5.0);
    
        assertEquals(5.0, result.getStars());
        assertEquals(ReviewStatus.WRITTEN, result.getReviewStatus());
        assertTrue(result.isFinished());
    }
    
    @Test
    public void userWritesReview_clearsExistingIgnoreUntil() {
        // Pre-existing cool-down must be cleared once the review is written.
        pendingReview.setIgnoreUntil(LocalDateTime.now().plusHours(10));
        Mockito.when(reviewRepository.findById("r1")).thenReturn(Optional.of(pendingReview));
    
        Review result = reviewService.userWritesReview("r1", "Solid experience", 4.0);
    
        assertNull(result.getIgnoreUntil());
        assertEquals(ReviewStatus.WRITTEN, result.getReviewStatus());
    }
    
    // --- reviewPopupNecessary: iteration order ------------------------------
    
    @Test
    public void reviewPopupNecessary_skipsIneligibleAndReturnsFirstEligible() {
        Review finished = new Review();
        finished.setId("skip-finished");
        finished.setSender(sender);
        finished.setFinished(true);
        finished.setReviewStatus(ReviewStatus.WRITTEN);
    
        Review dismissed = new Review();
        dismissed.setId("skip-dismissed");
        dismissed.setSender(sender);
        dismissed.setReviewStatus(ReviewStatus.PENDING);
        dismissed.setFinished(false);
        dismissed.setIgnoreUntil(LocalDateTime.now().plusHours(5));
    
        Review eligible = new Review();
        eligible.setId("eligible");
        eligible.setSender(sender);
        eligible.setReviewStatus(ReviewStatus.PENDING);
        eligible.setFinished(false);
    
        Mockito.when(reviewRepository.findBySender(sender))
                .thenReturn(List.of(finished, dismissed, eligible));
    
        Review result = reviewService.reviewPopupNecessary(sender);
    
        assertNotNull(result);
        assertEquals("eligible", result.getId());
    }
    
    // --- fetchReceivedReviews: thin coverage --------------------------------
    
    @Test
    public void fetchReceivedReviews_emptyList_returnsEmpty() {
        Mockito.when(reviewRepository.findByReceiver(sender))
                .thenReturn(Collections.emptyList());
    
        assertTrue(reviewService.fetchReceivedReviews(sender).isEmpty());
    }
    
    @Test
    public void fetchReceivedReviews_noWrittenReviews_returnsEmpty() {
        Review pending = new Review();
        pending.setId("rp");
        pending.setReviewStatus(ReviewStatus.PENDING);
    
        Review ignored = new Review();
        ignored.setId("ri");
        ignored.setReviewStatus(ReviewStatus.IGNORED);
    
        Review hasnotHappened = new Review();
        hasnotHappened.setId("rh");
        hasnotHappened.setReviewStatus(ReviewStatus.HASNOTHAPPENED);
    
        Mockito.when(reviewRepository.findByReceiver(sender))
                .thenReturn(List.of(pending, ignored, hasnotHappened));
    
        assertTrue(reviewService.fetchReceivedReviews(sender).isEmpty());
    }



}
