package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for UserService using a real H2 in-memory database.
 * Tests the full create-and-login flow without mocking.
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    /**
     * Helper: creates a fully populated User with all required registration fields.
     */
    private User createFullUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("testPassword");
        user.setSurname("John");
        user.setLastname("Doe");
        user.setEmailAddress(email);
        user.setIsVolunteer(false);
        user.setBio("Test bio");
        user.setAddress("Test Street 1");
        user.setGender("male");
        user.setPhoneNumber("+41 79 000 00 00");
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));
        return user;
    }

    @Test
    public void createUser_validInputs_success() {
        // given: no users in the database
        assertNull(userRepository.findByUsername("testUser"));

        User testUser = createFullUser("testUser", "john@example.com");

        // when: creating the user
        User createdUser = userService.createUser(testUser);

        // then: user is persisted and auto-logged-in (has a token)
        assertEquals("testUser", createdUser.getUsername());
        assertEquals("John", createdUser.getSurname());
        assertEquals("Doe", createdUser.getLastname());
        assertNotNull(createdUser.getToken());
        assertFalse(createdUser.getIsVolunteer());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // given: a user already exists with this username
        assertNull(userRepository.findByUsername("testUser"));

        User firstUser = createFullUser("testUser", "first@example.com");
        userService.createUser(firstUser);

        // when: trying to create a second user with the same username
        User secondUser = createFullUser("testUser", "second@example.com");

        // then: a ResponseStatusException is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(secondUser));
    }

    @Test
    public void createUser_duplicateEmail_throwsException() {
        // given: a user already exists with this email
        User firstUser = createFullUser("firstUser", "same@example.com");
        userService.createUser(firstUser);

        // when: trying to create a second user with the same email
        User secondUser = createFullUser("secondUser", "same@example.com");

        // then: a ResponseStatusException is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(secondUser));
    }
}
