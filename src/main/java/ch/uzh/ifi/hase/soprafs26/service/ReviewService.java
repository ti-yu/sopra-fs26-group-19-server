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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

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
        Review review = new Review();
        review.setSender(sender);
        review.setReceiver(receiver);
        review.setInserat(inserat);
        review.setCreationDate(LocalDate.now());
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

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        Inserat inserat = review.getInserat();
        LocalDate inseratDate = inserat.getDate();
        LocalTime inseratTime = null;
        if (inserat.getTime() != null && !inserat.getTime().trim().isEmpty()) {
            inseratTime = LocalTime.parse(inserat.getTime());
        }


        if (review.getText() != null && !review.getText().isEmpty()) {
            review.setReviewStatus(ReviewStatus.WRITTEN);
            review.setFinished(true);
        } else if (inseratDate.isAfter(currentDate)) {
            review.setReviewStatus(ReviewStatus.HASNOTHAPPENED);
        } else if (inseratDate.isEqual(currentDate)) {
            if (inseratTime != null && inseratTime.isAfter(currentTime.plusHours(1))) {
                review.setReviewStatus(ReviewStatus.HASNOTHAPPENED);
            } else {
                review.setReviewStatus(ReviewStatus.PENDING);
            }
        } else if (currentDate.isAfter(inseratDate.plusMonths(3))) {
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

    public Review userWritesReview(String reviewId, String text) {
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

        review.setText(text);
        review.setReviewStatus(ReviewStatus.WRITTEN);
        review.setFinished(true);

        review = reviewRepository.save(review);
        reviewRepository.flush();

        log.debug("Review {} written by user", reviewId);
        return review;
    }

    public Review reviewPopupNecessary(User user) {
        List<Review> reviewList = reviewRepository.findBySender(user);
        for (Review review : reviewList) {
            updateReviewStatusBasedOnObjective(review);
            if (review.getReviewStatus() == ReviewStatus.PENDING) {
                return review;
            }
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