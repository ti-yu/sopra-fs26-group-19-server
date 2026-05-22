package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests that DTOMapper correctly maps between User entity and DTOs.
 * Verifies that all fields (including the boolean isVolunteer) are
 * properly transferred in both directions.
 */
public class DTOMapperTest {

    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // create a UserPostDTO with all fields set
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("testPassword");
        userPostDTO.setSurname("John");
        userPostDTO.setLastname("Doe");
        userPostDTO.setEmailAddress("john@example.com");
        userPostDTO.setIsVolunteer(true);
        userPostDTO.setBio("A bio");
        userPostDTO.setAddress("Test Street 1");
        userPostDTO.setGender("male");
        userPostDTO.setPhoneNumber("+41 79 000 00 00");
        userPostDTO.setDateOfBirth(LocalDate.of(2000, 1, 15));

        // MAP: PostDTO -> User entity
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // Verify all fields are mapped correctly
        assertEquals("testUser", user.getUsername());
        assertEquals("testPassword", user.getPassword());
        assertEquals("John", user.getSurname());
        assertEquals("Doe", user.getLastname());
        assertEquals("john@example.com", user.getEmailAddress());
        assertEquals(true, user.getIsVolunteer());
        assertEquals("A bio", user.getBio());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create a User entity with all fields set
        User user = new User();
        user.setId("test-uuid-123");
        user.setUsername("testUser");
        user.setSurname("John");
        user.setLastname("Doe");
        user.setEmailAddress("john@example.com");
        user.setIsVolunteer(false);
        user.setBio("A bio");
        user.setGender("male");
        user.setToken("test-token");
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));

        // MAP: User entity -> GetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // Verify all fields are mapped correctly
        assertEquals("test-uuid-123", userGetDTO.getId());
        assertEquals("testUser", userGetDTO.getUsername());
        assertEquals("John", userGetDTO.getSurname());
        assertEquals("Doe", userGetDTO.getLastname());
        assertEquals("john@example.com", userGetDTO.getEmailAddress());
        assertEquals(false, userGetDTO.getIsVolunteer());
        assertEquals("A bio", userGetDTO.getBio());
        assertEquals("test-token", userGetDTO.getToken());
    }


    @Test
    public void testGetReview_fromReview_toReviewGetDTO_success() {
        // create sender User
        User sender = new User();
        sender.setId("sender-uuid-1");
        sender.setUsername("alice");
 
        // create receiver User
        User receiver = new User();
        receiver.setId("receiver-uuid-2");
        receiver.setUsername("bob");
 
        // create linked Inserat
        Inserat inserat = new Inserat();
        inserat.setDescription("Help with shopping");
        inserat.setLocation("Zurich");
 
        // create Review entity with all fields set
        Review review = new Review();
        review.setId("review-uuid-123");
        review.setSender(sender);
        review.setReceiver(receiver);
        review.setInserat(inserat);
        review.setText("Great help, thank you!");
        review.setStars(4.5);
        review.setReviewStatus(ReviewStatus.WRITTEN);
        review.setFinished(true);
 
        // MAP: Review entity -> ReviewGetDTO
        ReviewGetDTO dto = DTOMapper.INSTANCE.convertEntityToReviewGetDTO(review);
 
        // Verify direct fields
        assertEquals("review-uuid-123", dto.getId());
        assertEquals("Great help, thank you!", dto.getText());
        assertEquals(4.5, dto.getStars());
        assertEquals(ReviewStatus.WRITTEN, dto.getReviewStatus());
 
        // Verify flattened sender fields
        assertEquals("sender-uuid-1", dto.getSenderId());
        assertEquals("alice", dto.getSenderUsername());
 
        // Verify flattened receiver fields
        assertEquals("receiver-uuid-2", dto.getReceiverId());
        assertEquals("bob", dto.getReceiverUsername());
 
        // Verify flattened inserat fields
        assertEquals("Help with shopping", dto.getInseratDescription());
        assertEquals("Zurich", dto.getInseratLocation());
    }
 
    @Test
    public void testGetReview_fromReview_toReviewGetDTO_nullNestedObjects_returnsNullFields() {
        // Defensive case: a freshly created review may have no inserat yet,
        // and MapStruct must not NPE on the flattened source paths.
        Review review = new Review();
        review.setId("review-uuid-bare");
        review.setText("");
        review.setReviewStatus(ReviewStatus.PENDING);
        // sender, receiver, inserat intentionally left null
 
        ReviewGetDTO dto = DTOMapper.INSTANCE.convertEntityToReviewGetDTO(review);
 
        assertEquals("review-uuid-bare", dto.getId());
        assertEquals(ReviewStatus.PENDING, dto.getReviewStatus());
        assertNull(dto.getSenderId());
        assertNull(dto.getSenderUsername());
        assertNull(dto.getReceiverId());
        assertNull(dto.getReceiverUsername());
        assertNull(dto.getInseratDescription());
        assertNull(dto.getInseratLocation());
    }
 
    @Test
    public void testCreateReview_fromReviewPostDTO_toReview_success() {
        // create a ReviewPostDTO with the fields the client supplies
        ReviewPostDTO postDTO = new ReviewPostDTO();
        postDTO.setText("Excellent service");
        postDTO.setStars(5.0);
 
        // MAP: ReviewPostDTO -> Review entity
        Review review = DTOMapper.INSTANCE.convertReviewPostDTOtoEntity(postDTO);
 
        // Verify the supplied fields are carried over
        assertEquals("Excellent service", review.getText());
        assertEquals(5.0, review.getStars());
    }

}
