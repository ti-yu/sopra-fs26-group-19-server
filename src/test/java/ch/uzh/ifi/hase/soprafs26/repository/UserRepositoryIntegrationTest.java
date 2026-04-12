package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration test for UserRepository using @DataJpaTest (H2 in-memory DB).
 * Tests the custom query methods findByUsername and findByEmailAddress.
 */
@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper: creates and persists a fully populated User entity.
     * All non-nullable columns must be set for the entity to be valid.
     */
    private User persistTestUser() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setSurname("John");
        user.setLastname("Doe");
        user.setEmailAddress("john@example.com");
        user.setIsVolunteer(true);
        user.setBio("A bio");
        user.setAddress("Some Street 1");
        user.setGender("male");
        user.setPhoneNumber("+41 79 000 00 00");
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));
        user.setToken("test-token");

        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    @Test
    public void findByUsername_success() {
        // given: a user is persisted in the database
        User user = persistTestUser();

        // when: searching by username
        User found = userRepository.findByUsername(user.getUsername());

        // then: the correct user is returned with all fields intact
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals("testUser", found.getUsername());
        assertEquals("John", found.getSurname());
        assertEquals("Doe", found.getLastname());
        assertEquals("john@example.com", found.getEmailAddress());
        assertEquals(true, found.getIsVolunteer());
    }

    @Test
    public void findByEmailAddress_success() {
        // given: a user is persisted in the database
        User user = persistTestUser();

        // when: searching by email address
        User found = userRepository.findByEmailAddress(user.getEmailAddress());

        // then: the correct user is returned
        assertNotNull(found);
        assertEquals("testUser", found.getUsername());
        assertEquals("john@example.com", found.getEmailAddress());
    }

    @Test
    public void findByUsername_notFound_returnsNull() {
        // when: searching for a username that doesn't exist
        User found = userRepository.findByUsername("nonExistentUser");

        // then: null is returned
        assertNull(found);
    }
}
