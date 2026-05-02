package ch.uzh.ifi.hase.soprafs26.rest.dto;

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
}