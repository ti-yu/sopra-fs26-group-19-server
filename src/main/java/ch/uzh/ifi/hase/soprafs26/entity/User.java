package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
$
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;



@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private boolean isVolunteer;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String lastname;

    /**
     * Store a hash, not a plain password.
     * The UML says Password:str, but storing raw passwords would be a bad design.
     */
    @Column(nullable = false)
    private String password;

    @Column(length = 2000)
    private String bio;

    /**
     * UML says ProfilePicture: "image".
     * In a real system, storing a URL/path is usually better than storing raw image bytes.
     * If you really want binary image storage, replace this with @Lob private byte[] profilePicture;
     */
    @Column
    private String profilePicture;

    @Column
    private String address;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private String gender;

    @Column
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String emailAddress;

    /**
     * PreviousInserat: list[Inserat]
     * Assuming Inserat has a field like: private User recipient;
     */
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Inserat> previousInserat = new ArrayList<>();

    /**
     * Reviews: list[Review]
     * UML shows user receiving 0..* reviews.
     * Assuming Review has a field like: private User receiver;
     */
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Review> reviews = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isVolunteer() {
        return isVolunteer;
    }

    public void setVolunteer(boolean volunteer) {
        isVolunteer = volunteer;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<Inserat> getPreviousInserat() {
        return previousInserat;
    }

    public void setPreviousInserat(List<Inserat> previousInserat) {
        this.previousInserat = previousInserat;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}