package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ApplicantDTO {

    private String id;
    private String username;
    private LocalDate dateOfBirth;
}

