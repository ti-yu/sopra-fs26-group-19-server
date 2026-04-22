package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InseratGetDTO {
    private String id;
    private String recipientId;
    private String recipientUsername;
    private String recipientSurname;
    private String recipientLastname;
    private int recipientAge;
    private String recipientPhone;
    private String recipientEmail;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDate date;
    private String timeframe;
    private InseratStatus status;
    private LocalDate creationDate;
    private int volunteerAppliedCount;
    private String volunteerAcceptedUsername;
    private String volunteerAcceptedPhone;
    private String volunteerAcceptedEmail;
    private String workType;
}
