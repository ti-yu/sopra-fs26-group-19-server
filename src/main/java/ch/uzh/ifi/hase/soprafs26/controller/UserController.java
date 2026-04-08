package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
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
}