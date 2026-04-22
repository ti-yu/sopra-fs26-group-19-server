package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserPostDTO {

    private String username;
    private String password;
    private String surname;
    private String lastname;
    private String emailAddress;
    private Boolean volunteer;
    private String bio;
    private String address;
    private String gender;
    private String phoneNumber;
    private LocalDate dateOfBirth;
}
