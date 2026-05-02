package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Review;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReviewPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ReviewService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    UserController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @GetMapping("/profile/{id}/pendingReview")
    @ResponseBody
    public ResponseEntity<ReviewGetDTO> getPendingReview(@PathVariable String id) {
        User user = userService.getUserById(id);
        Review review = reviewService.reviewPopupNecessary(user);
        if (review == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToReviewGetDTO(review));
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

    @PutMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateUser(@PathVariable String id, @RequestBody UserPutDTO userPutDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        userService.updateUser(id, userInput);
    }

    @PostMapping("/profile/{id}/reviews/{reviewId}/write")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ReviewGetDTO writeReview(@PathVariable String reviewId, @RequestBody ReviewPostDTO dto) {
        Review review = reviewService.userWritesReview(reviewId, dto.getText());
        return DTOMapper.INSTANCE.convertEntityToReviewGetDTO(review);
    }

    @PostMapping("/profile/{id}/reviews/{reviewId}/ignore")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ReviewGetDTO ignoreReview(@PathVariable String reviewId) {
        Review review = reviewService.userIgnoresReview(reviewId);
        return DTOMapper.INSTANCE.convertEntityToReviewGetDTO(review);
    }

    @GetMapping("/profile/{id}/reviews/done")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ReviewGetDTO> getDoneReviews(@PathVariable String id) {
        User user = userService.getUserById(id);
        List<Review> reviews = reviewService.fetchDoneReviews(user);
        return reviews.stream()
            .map(DTOMapper.INSTANCE::convertEntityToReviewGetDTO)
            .collect(Collectors.toList());
    }
}