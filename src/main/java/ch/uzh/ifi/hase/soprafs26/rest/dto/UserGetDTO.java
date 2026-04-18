package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

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
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getIsVolunteer() {
        return isVolunteer;
    }

    public void setIsVolunteer(Boolean isVolunteer) {
        this.isVolunteer = isVolunteer;
    }
}
