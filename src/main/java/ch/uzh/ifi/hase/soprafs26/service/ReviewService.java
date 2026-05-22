package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ReviewRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private static final ZoneId ZURICH_ZONE = ZoneId.of("Europe/Zurich");

    public ReviewService(
        @Qualifier("reviewRepository") ReviewRepository reviewRepository,
        @Qualifier("userRepository") UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    private User checkUserExists(String userId) {
        return userRepository.findById(userId).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The user with id " + userId + " was not found!"
            )
        );
    }

    public List<Review> getReviewsBySenderId(String senderId) {
        User sender = checkUserExists(senderId);
        return reviewRepository.findBySender(sender);
    }

    public List<Review> getReviewsByReceiverId(String receiverId) {
        User receiver = checkUserExists(receiverId);
        return reviewRepository.findByReceiver(receiver);
    }

    public Review acceptedInseratCreateReview(User sender, User receiver, Inserat inserat) {
        if (reviewRepository.existsBySenderAndInserat(sender, inserat)) {
            return null;
        }

        Review review = new Review();
        review.setSender(sender);
        review.setReceiver(receiver);
        review.setInserat(inserat);
        review.setCreationDate(LocalDate.now(ZURICH_ZONE));
        review.setFinished(false);
        review.setText("");                       // placeholder, filled in when sender writes it
        review.setReviewStatus(ReviewStatus.HASNOTHAPPENED);

        updateReviewStatusBasedOnObjective(review);

        review = reviewRepository.save(review);
        reviewRepository.flush();

        log.debug("Created pending review from {} to {} for inserat {}",
                sender.getId(), receiver.getId(), inserat.getId());
        return review;
    }


    public ReviewStatus updateReviewStatusBasedOnObjective(Review review) {
        if (review.isFinished()) {
            return review.getReviewStatus();
        }

        if (review.getReviewStatus() == ReviewStatus.IGNORED) {
            review.setFinished(true);
            reviewRepository.save(review);
            reviewRepository.flush();
            return review.getReviewStatus();
        }

        LocalDateTime now = LocalDateTime.now(ZURICH_ZONE);
        LocalDate currentDate = now.toLocalDate();

        Inserat inserat = review.getInserat();
        LocalDate inseratDate = inserat.getDate();
        LocalTime inseratTime = null;
        if (inserat.getTime() != null && !inserat.getTime().trim().isEmpty()) {
            inseratTime = LocalTime.parse(inserat.getTime());
        }

        // Build the inserat's END LocalDateTime so the comparison is correct
        // even when a session crosses midnight. If no start time was given we
        // treat the request as ending at the very end of its date.
        LocalDateTime inseratEnd;
        if (inseratTime != null) {
            double durationHours = 0.0;
            try {
                durationHours = Double.parseDouble(inserat.getTimeframe());
            } catch (NumberFormatException | NullPointerException ignored) {
                // Default to 0 (= ends at start time). Conservative.
            }
            long totalMinutes = (long) Math.round(durationHours * 60);
            inseratEnd = LocalDateTime.of(inseratDate, inseratTime).plusMinutes(totalMinutes);
        } else {
            inseratEnd = LocalDateTime.of(inseratDate, LocalTime.MAX);
        }

        if (review.getText() != null && !review.getText().isEmpty()) {
            review.setReviewStatus(ReviewStatus.WRITTEN);
            review.setFinished(true);
        } else if (inseratEnd.isAfter(now)) {
            // Session has not yet finished.
            review.setReviewStatus(ReviewStatus.HASNOTHAPPENED);
        } else if (currentDate.isAfter(inseratDate.plusMonths(3))) {
            // Stale unwritten reviews drop out after 3 months.
            review.setReviewStatus(ReviewStatus.IGNORED);
            review.setFinished(true);
        } else {
            review.setReviewStatus(ReviewStatus.PENDING);
        }

        reviewRepository.save(review);
        reviewRepository.flush();
        return review.getReviewStatus();
    }

    public Review userIgnoresReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setReviewStatus(ReviewStatus.IGNORED);
        review.setFinished(true);

        review = reviewRepository.save(review);
        reviewRepository.flush();

        log.debug("Review {} marked as IGNORED by user", reviewId);
        return review;
    }

    public Review userWritesReview(String reviewId, String text, Double stars) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (text == null || text.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text must not be empty");
        }
        if (text.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text must be at most 100 characters");
        }
        if (review.getReviewStatus() == ReviewStatus.IGNORED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot write a review that was ignored");
        }
        if (stars == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Star rating is required");
        }
        if (stars < 0.5 || stars > 5.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stars must be between 0.5 and 5.0");
        }
        // Ensure half-step granularity (0.5, 1.0, 1.5, ..., 5.0)
        double doubled = stars * 2;
        if (doubled != Math.floor(doubled)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stars must be in 0.5 increments");
        }

        review.setText(text);
        review.setStars(stars);
        review.setReviewStatus(ReviewStatus.WRITTEN);
        review.setFinished(true);
        // Clear any "ignore for now" timestamp now that the review is written.
        review.setIgnoreUntil(null);

        review = reviewRepository.save(review);
        reviewRepository.flush();

        log.debug("Review {} written by user", reviewId);
        return review;
    }

    /**
     * Issue #151: when the user dismisses the popup with "Ignore for now",
     * push the next reminder out by 24 hours instead of clearing it forever.
     * Status stays PENDING; only the ignoreUntil timestamp moves.
     */
    public Review dismissReviewForNow(String reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (review.isFinished()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot dismiss a finished review");
        }

        review.setIgnoreUntil(LocalDateTime.now(ZURICH_ZONE).plusHours(24));

        review = reviewRepository.save(review);
        reviewRepository.flush();

        log.debug("Review {} ignored for 24h", reviewId);
        return review;
    }

    public Review reviewPopupNecessary(User user) {
        List<Review> reviewList = reviewRepository.findBySender(user);
        LocalDateTime now = LocalDateTime.now(ZURICH_ZONE);
        for (Review review : reviewList) {
            // Finished reviews (written or ignored) never re-trigger the popup.
            if (review.isFinished()) {
                continue;
            }
            // Refresh the status from the current time vs. inserat schedule.
            // Only PENDING reviews (inserat already ended) should pop up.
            updateReviewStatusBasedOnObjective(review);
            if (review.getReviewStatus() != ReviewStatus.PENDING) {
                continue;
            }
            // Issue #151: respect "ignore for now" (24h cool-down).
            if (review.getIgnoreUntil() != null && review.getIgnoreUntil().isAfter(now)) {
                continue;
            }
            return review;
        }
        return null;
    }

    public List<Review> fetchDoneReviews(User user) {
        List<Review> reviewList = reviewRepository.findBySender(user);
        List<Review> doneReviews = new ArrayList<>();
        for (Review review : reviewList) {
            updateReviewStatusBasedOnObjective(review);
            if (review.isFinished()) {
                doneReviews.add(review);
            }
        }
        return doneReviews;
    }

    public List<Review> fetchReceivedReviews(User user) {
        List<Review> reviewList = reviewRepository.findByReceiver(user);
        List<Review> receivedReviews = new ArrayList<>();
        for (Review review : reviewList) {
            // We only want to show reviews that were actually written
            if (review.getReviewStatus() == ReviewStatus.WRITTEN) {
                receivedReviews.add(review);
            }
        }
        return receivedReviews;
    }
}