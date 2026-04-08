package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.InseratService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/profile/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUserById(@PathVariable String id) {
		User user = userService.getUserById(id);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User authorizedUser = userService.loginUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(authorizedUser);
    }

    @GetMapping("/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserProfile(@PathVariable String id) {
        User foundUser = userService.getUserById(id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(foundUser);
    }

	@PostMapping("/help-requests")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public InseratGetDTO createInserat(@RequestBody InseratPostDTO inseratPostDTO) {
		Inserat newInserat = DTOMapper.INSTANCE.convertInseratPostDTOtoEntity(inseratPostDTO);
		Inserat createdInserat = inseratService.createInserat(newInserat);
		return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(createdInserat);
	}

	@GetMapping("/users/{id}/help-requests")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<InseratGetDTO> getInseratsByRecipientId(@PathVariable String id) {
		List<Inserat> inserats = inseratService.getInseratsByRecipientId(id);
		return inserats.stream()
			.map(DTOMapper.INSTANCE::convertEntityToInseratGetDTO)
			.collect(Collectors.toList());
	}
}
