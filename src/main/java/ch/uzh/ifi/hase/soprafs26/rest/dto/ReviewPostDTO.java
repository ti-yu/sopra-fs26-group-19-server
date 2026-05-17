package ch.uzh.ifi.hase.soprafs26.rest.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ReviewPostDTO {

    private String senderId;
    private String receiverId;
    private String inseratId;
    private String text;
    private Double stars;
}