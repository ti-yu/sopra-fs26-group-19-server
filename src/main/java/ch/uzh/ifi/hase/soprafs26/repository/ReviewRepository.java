package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("reviewRepository")
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findBySender(User sender);

    List<Review> findByReceiver(User receiver);

    boolean existsBySenderAndInserat(User sender, Inserat inserat);
}