package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserGetDTO {

    private String id;
    private String username;
    private String surname;
    private String lastname;
    private String emailAddress;
    private String phoneNumber;
    private String address;
    private String bio;
    private LocalDate dateOfBirth;
    private int age;
    private String gender;
    private String token;
    private Boolean isVolunteer;
}