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
        return newUser;
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
    String baseErrorMessage = "%s must not be empty. Therefore, the user could not be created!";

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

    if (isBlank(user.getSurname())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Surname")
        );
    }

    if (isBlank(user.getLastname())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Lastname")
        );
    }

    if (isBlank(user.getEmailAddress())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Email address")
        );
    }

    // Optional fields: only validate if you want them mandatory
    if (user.getDateOfBirth() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Date of birth")
        );
    }

    if (isBlank(user.getAddress())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Address")
        );
    }

    if (isBlank(user.getGender())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Gender")
        );
    }

    if (isBlank(user.getPhoneNumber())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Phone number")
        );
    }

    if (isBlank(user.getBio())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(baseErrorMessage, "Bio")
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
				HttpStatus.BAD_REQUEST,
				String.format(baseErrorMessage, "username and email", "are")
			);
		} else if (userByUsername != null) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				String.format(baseErrorMessage, "username", "is")
			);
		} else if (userByEmail != null) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				String.format(baseErrorMessage, "email", "is")
			);
		}
	}
}
