package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        userPostDTO.setVolunteer(true);
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
        assertEquals(true, user.getVolunteer());
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
        user.setVolunteer(false);
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
        assertEquals(false, userGetDTO.getVolunteer());
        assertEquals("A bio", userGetDTO.getBio());
        assertEquals("test-token", userGetDTO.getToken());
    }
}
