package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InseratPostDTO {

    private String id;
    private String recipientId;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDate date;
    private String timeframe;
    private String workType;
    private String time;
}