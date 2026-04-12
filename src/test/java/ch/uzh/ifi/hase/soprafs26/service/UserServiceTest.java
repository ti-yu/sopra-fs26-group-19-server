package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService using Mockito to mock the UserRepository.
 * Tests createUser and loginUser logic in isolation.
 */
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create a fully populated test user (all required fields for registration)
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");
        testUser.setSurname("John");
        testUser.setLastname("Doe");
        testUser.setEmailAddress("john@example.com");
        testUser.setIsVolunteer(false);
        testUser.setBio("Test bio");
        testUser.setAddress("Test Street 1");
        testUser.setGender("male");
        testUser.setPhoneNumber("+41 79 000 00 00");
        testUser.setDateOfBirth(LocalDate.of(2000, 1, 15));

        // When the repository saves any User, return testUser
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    public void createUser_validInputs_success() {
        // createUser flow: checkIfUserExists (findByUsername + findByEmailAddress)
        // → save → loginUser (findByUsername again to verify user exists).
        // So findByUsername is called twice: first for uniqueness (must return null),
        // then for login lookup (must return the saved user).
        Mockito.when(userRepository.findByUsername("testUser"))
                .thenReturn(null)       // first call: uniqueness check
                .thenReturn(testUser);  // second call: login lookup
        Mockito.when(userRepository.findByEmailAddress("john@example.com")).thenReturn(null);

        // when: creating the user (createUser also calls loginUser internally)
        User createdUser = userService.createUser(testUser);

        // then: user is saved and gets a token from auto-login
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals("testUser", createdUser.getUsername());
        assertEquals("John", createdUser.getSurname());
        assertNotNull(createdUser.getToken());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        // given: a user with the same username already exists
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        Mockito.when(userRepository.findByEmailAddress(Mockito.any())).thenReturn(null);

        // then: creating a user with a duplicate username throws an exception
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void createUser_duplicateEmail_throwsException() {
        // given: a user with the same email already exists
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
        Mockito.when(userRepository.findByEmailAddress("john@example.com")).thenReturn(testUser);

        // then: creating a user with a duplicate email throws an exception
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void loginUser_validCredentials_success() {
        // given: user exists in the database with matching password
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User loginInput = new User();
        loginInput.setUsername("testUser");
        loginInput.setPassword("testPassword");

        // when: logging in
        User loggedInUser = userService.loginUser(loginInput);

        // then: a token is generated
        assertNotNull(loggedInUser.getToken());
        assertEquals("testUser", loggedInUser.getUsername());
    }

    @Test
    public void loginUser_wrongPassword_throwsException() {
        // given: user exists but we provide wrong password
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User loginInput = new User();
        loginInput.setUsername("testUser");
        loginInput.setPassword("wrongPassword");

        // then: login with wrong password throws UNAUTHORIZED
        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }

    @Test
    public void loginUser_nonExistentUser_throwsException() {
        // given: no user with this username exists
        Mockito.when(userRepository.findByUsername("ghostUser")).thenReturn(null);

        User loginInput = new User();
        loginInput.setUsername("ghostUser");
        loginInput.setPassword("anyPassword");

        // then: login with non-existent username throws NOT_FOUND
        assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
    }
}
