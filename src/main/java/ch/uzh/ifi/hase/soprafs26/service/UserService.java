package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

    public User getUserById(String id) {
        return this.userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The user with id " + id + " was not found!"
                )
        );
    }

	// helper:
	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	//generally: this is registration logic:
    public User createUser(User newUser) {

        normalizeUserInput(newUser);
        checkValidRegistrationData(newUser);
        checkIfUserExists(newUser);

        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created user: {}", newUser);
        return loginUser(newUser); // ✅ auto-login sets the token
    }

    private void normalizeUserInput(User user) {
        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim());
        }
        if (user.getSurname() != null) {
            user.setSurname(user.getSurname().trim());
        }
        if (user.getLastname() != null) {
            user.setLastname(user.getLastname().trim());
        }
        if (user.getPassword() != null) {
            user.setPassword(user.getPassword().trim());
        }
        if (user.getBio() != null) {
            user.setBio(user.getBio().trim());
        }
        if (user.getAddress() != null) {
            user.setAddress(user.getAddress().trim());
        }
        if (user.getGender() != null) {
            user.setGender(user.getGender().trim());
        }
        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }
        if (user.getEmailAddress() != null) {
            user.setEmailAddress(user.getEmailAddress().trim());
        }
    }

    private void checkValidRegistrationData(User user) {
        boolean hasMissingData = isBlank(user.getUsername()) ||
                isBlank(user.getPassword()) ||
                isBlank(user.getSurname()) ||
                isBlank(user.getLastname()) ||
                isBlank(user.getEmailAddress());
                // just add other fields if u want them to be mandatory with same logic

        if (hasMissingData) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more mandatory fields are empty. Therefore, the user could not be created!"
            );
        }
    }



	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByEmail = userRepository.findByEmailAddress(userToBeCreated.getEmailAddress());

		String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";

		if (userByUsername != null && userByEmail != null) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				String.format(baseErrorMessage, "username and email", "are")
			);
		} else if (userByUsername != null) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				String.format(baseErrorMessage, "username", "is")
			);
		} else if (userByEmail != null) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				String.format(baseErrorMessage, "email", "is")
			);
		}
	}
    //generally: this is login logic:
    public User loginUser(User userInput) {
        //Validate that the frontend actually sent a username and password
        checkValidLoginData(userInput);

        //Find the user in the database
        User existingUser = userRepository.findByUsername(userInput.getUsername().trim());

        // Check if the user exists
        if (existingUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The username provided does not exist!"
            );
        }

        // 4. Check if the password matches
        if (!existingUser.getPassword().equals(userInput.getPassword().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "The password provided is incorrect!"
            );
        }
        existingUser.setToken(UUID.randomUUID().toString());

        log.debug("Logged in user: {}", existingUser);
        return existingUser;
    }

    private void checkValidLoginData(User user) {
        String baseErrorMessage = "%s must not be empty. Therefore, the user could not be logged in!";

        if (isBlank(user.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage, "Username")
            );
        }

        if (isBlank(user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage, "Password")
            );
        }

    }

    public void updateUser(String id, User userInput) {
        User existingUser = getUserById(id);

        if (!isBlank(userInput.getUsername())) {
            User userByUsername = userRepository.findByUsername(userInput.getUsername().trim());
            if (userByUsername != null && !userByUsername.getId().equals(existingUser.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken!");
            }
            existingUser.setUsername(userInput.getUsername().trim());
        }

        if (!isBlank(userInput.getPassword())) {
            existingUser.setPassword(userInput.getPassword().trim());
        }

        if (!isBlank(userInput.getSurname())) {
            existingUser.setSurname(userInput.getSurname().trim());
        }

        if (!isBlank(userInput.getLastname())) {
            existingUser.setLastname(userInput.getLastname().trim());
        }

        if (!isBlank(userInput.getEmailAddress())) {
            User userByEmail = userRepository.findByEmailAddress(userInput.getEmailAddress().trim());
            if (userByEmail != null && !userByEmail.getId().equals(existingUser.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email address is already taken!");
            }
            existingUser.setEmailAddress(userInput.getEmailAddress().trim());
        }

        existingUser.setIsVolunteer(userInput.getIsVolunteer());

        if (!isBlank(userInput.getBio())) {
            existingUser.setBio(userInput.getBio().trim());
        }

        if (!isBlank(userInput.getAddress())) {
            existingUser.setAddress(userInput.getAddress().trim());
        }

        if (!isBlank(userInput.getGender())) {
            existingUser.setGender(userInput.getGender().trim());
        }

        if (!isBlank(userInput.getPhoneNumber())) {
            existingUser.setPhoneNumber(userInput.getPhoneNumber().trim());
        }

        if (userInput.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(userInput.getDateOfBirth());
        }

        userRepository.save(existingUser);
        userRepository.flush();
        log.debug("Updated user: {}", existingUser);
    }
}
