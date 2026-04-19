package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("inseratRepository")
public interface InseratRepository extends JpaRepository<Inserat, String> {
    List<Inserat> findByRecipient(User recipient);

    List<Inserat> findByVolunteerAppliedContaining(User volunteer);
}