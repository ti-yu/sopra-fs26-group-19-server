package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PasswordChangeDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.ReviewService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * Tests the UserController REST endpoints (GET /profile/{id}, POST /register,
 * POST /login) using MockMvc without starting a real server.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReviewService reviewService;

    /**
     * Helper: creates a fully populated User entity for testing.
     * All required fields are set to avoid null pointer issues in assertions.
     */
    private User createTestUser() {
        User user = new User();
        user.setId("test-uuid-123");
        user.setUsername("testUser");
        user.setSurname("John");
        user.setLastname("Doe");
        user.setPassword("securePassword");
        user.setEmailAddress("john@example.com");
        user.setIsVolunteer(false);
        user.setBio("A short bio");
        user.setAddress("Zürichstrasse 1");
        user.setGender("male");
        user.setPhoneNumber("+41 79 123 45 67");
        user.setDateOfBirth(java.time.LocalDate.of(2000, 1, 15));
        user.setToken("random-token-abc");
        return user;
    }

    @Test
    public void getProfile_validId_returnsUser() throws Exception {
        // given: a user exists in the service
        User user = createTestUser();
        given(userService.getUserById("test-uuid-123")).willReturn(user);

        // when: GET /profile/{id}
        MockHttpServletRequestBuilder getRequest = get("/profile/test-uuid-123")
                .contentType(MediaType.APPLICATION_JSON);

        // then: response contains the user's fields
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("test-uuid-123")))
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.surname", is("John")))
                .andExpect(jsonPath("$.lastname", is("Doe")))
                .andExpect(jsonPath("$.isVolunteer", is(false)));
    }

    @Test
    public void register_validInput_userCreated() throws Exception {
        // given: service will return a created user with a token
        User createdUser = createTestUser();

        UserPostDTO postDTO = new UserPostDTO();
        postDTO.setUsername("testUser");
        postDTO.setPassword("securePassword");
        postDTO.setSurname("John");
        postDTO.setLastname("Doe");
        postDTO.setEmailAddress("john@example.com");
        postDTO.setIsVolunteer(false);

        given(userService.createUser(Mockito.any())).willReturn(createdUser);

        // when: POST /register
        MockHttpServletRequestBuilder postRequest = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO));

        // then: status 201 and response contains the user data
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("test-uuid-123")))
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.token", is("random-token-abc")));
    }

    @Test
    public void login_validCredentials_returnsUser() throws Exception {
        // given: login returns an authorized user with a fresh token
        User authorizedUser = createTestUser();
        authorizedUser.setToken("fresh-login-token");

        given(userService.loginUser(Mockito.any())).willReturn(authorizedUser);

        UserPostDTO loginDTO = new UserPostDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("securePassword");

        // when: POST /login
        MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginDTO));

        // then: status 200 and response contains a token
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.token", is("fresh-login-token")));
    }

    @Test
    public void login_invalidUsername_returns404() throws Exception {
        // given: service throws NOT_FOUND for unknown username
        given(userService.loginUser(Mockito.any())).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "The username provided does not exist!"));

        UserPostDTO loginDTO = new UserPostDTO();
        loginDTO.setUsername("nonExistentUser");
        loginDTO.setPassword("anyPassword");

        // when: POST /login
        MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginDTO));

        // then: status 404
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    // --- change-password endpoint -----------------------------------------

    @Test
    public void changePassword_validInput_returns200() throws Exception {
        // service does its work silently on success
        Mockito.doNothing().when(userService)
                .changePassword(Mockito.eq("u1"), Mockito.eq("oldPwd"), Mockito.eq("newPwdLong"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("oldPwd");
        dto.setNewPassword("newPwdLong");

        mockMvc.perform(post("/profile/u1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword_wrongOldPassword_returns401() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect"))
                .when(userService).changePassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("wrong");
        dto.setNewPassword("anything123");

        mockMvc.perform(post("/profile/u1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void changePassword_weakNew_returns400() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters"))
                .when(userService).changePassword(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("oldPwd");
        dto.setNewPassword("abc");

        mockMvc.perform(post("/profile/u1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- dismiss-for-now endpoint -----------------------------------------

    @Test
    public void dismissReviewForNow_returnsUpdatedReview() throws Exception {
        // Build a minimally populated review the mapper can serialise.
        Review review = new Review();
        review.setId("rev-1");
        User sender = new User();
        sender.setId("u1");
        sender.setUsername("alice");
        User receiver = new User();
        receiver.setId("u2");
        receiver.setUsername("bob");
        review.setSender(sender);
        review.setReceiver(receiver);
        review.setText("");
        review.setReviewStatus(ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus.PENDING);

        given(reviewService.dismissReviewForNow("rev-1")).willReturn(review);

        mockMvc.perform(post("/profile/u1/reviews/rev-1/dismiss-for-now")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("rev-1")))
                .andExpect(jsonPath("$.reviewStatus", is("PENDING")));
    }

    /**
     * Converts an object to its JSON string representation for use as request body.
     * Uses Jackson's ObjectMapper (tools.jackson package in Spring Boot 4.0).
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
