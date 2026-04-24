package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
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
    public void getUserById_success() {
        //when
        Mockito.when(userRepository.findById("1")).thenReturn(java.util.Optional.of(testUser));

        //when
        User foundUser = userService.getUserById("1");

        //then
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    public void getUserById_notFound_throwsException() {
        // given
        Mockito.when(userRepository.findById("invalid-id")).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(ResponseStatusException.class, () -> userService.getUserById("invalid-id"));
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
    public void createUser_missingMandatoryData_throwsBadRequest() {
        // given: a user missing a mandatory field (username is null)
        User invalidUser = new User();
        invalidUser.setPassword("password");
        invalidUser.setSurname("John");
        invalidUser.setLastname("Doe");
        invalidUser.setEmailAddress("john@email.com");

        // then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(invalidUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
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
    public void createUser_duplicateUsernameAndEmail_throwsConflict() {
        // given: usernamee and email exist
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);
        Mockito.when(userRepository.findByEmailAddress(Mockito.anyString())).thenReturn(testUser);

        // then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
        assertTrue(exception.getReason().contains("username and email"));
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
    public void loginUser_missingData_throwsBadRequest() {
        //gien: missing password
        User invalidLogin = new User();
        invalidLogin.setUsername("testUser");

        //then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.loginUser(invalidLogin));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
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

    @Test
    public void updateUser_validInputs_success() {
        //given user exist in db
        testUser.setId("1");
        Mockito.when(userRepository.findById("1")).thenReturn(java.util.Optional.of(testUser));

        // No conflicts
        Mockito.when(userRepository.findByUsername("newUsername")).thenReturn(null);
        Mockito.when(userRepository.findByEmailAddress("new@email.com")).thenReturn(null);

        // User input with changes
        User updateInput = new User();
        updateInput.setUsername("newUsername");
        updateInput.setEmailAddress("new@email.com");
        updateInput.setBio("Updated bio");

        // when
        userService.updateUser("1", updateInput);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
        assertEquals("newUsername", testUser.getUsername());
        assertEquals("new@email.com", testUser.getEmailAddress());
        assertEquals("Updated bio", testUser.getBio());
    }

    @Test
    public void updateUser_duplicateUsername_throwsConflict() {
        // given
        testUser.setId("1");
        Mockito.when(userRepository.findById("1")).thenReturn(java.util.Optional.of(testUser));

        // another user holds the username we want
        User otherUser = new User();
        otherUser.setId("2");
        otherUser.setUsername("takenUsername");
        Mockito.when(userRepository.findByUsername("takenUsername")).thenReturn(otherUser);

        User updateInput = new User();
        updateInput.setUsername("takenUsername");

        // then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser("1", updateInput));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Username is already taken"));
    }
}
