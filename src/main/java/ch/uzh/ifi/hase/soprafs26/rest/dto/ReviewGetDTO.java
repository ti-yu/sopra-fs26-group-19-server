package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReviewGetDTO {
    private String id;
    private String senderId;
    private String receiverId;
    private String inseratId;
    private String text;
    private LocalDate creationDate;
    private String receiverUsername;
    private String inseratDescription;
    private String inseratLocation;
    private ReviewStatus reviewStatus;
}