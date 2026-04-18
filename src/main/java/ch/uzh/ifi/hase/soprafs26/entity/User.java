package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Period;



@Entity
@Table(name = "users")
public class User implements Serializable {
    
    public int getAge() {
    if (this.dateOfBirth == null) return 0;
    return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id = UUID.randomUUID().toString();

	@Column(nullable = true, unique = true)
	private String token;

    @Column(nullable = false)
    private boolean isVolunteer;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private String password;

    @Column(length = 2000)
    private String bio;

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

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Inserat> previousInserat = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Review> sentReviews = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Review> receivedReviews = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}


    // Using getIsVolunteer/setIsVolunteer naming so that Jackson correctly
    // serializes this field as "isVolunteer" in JSON (not "volunteer").
    // The standard JavaBeans convention for boolean isX() would make Jackson
    // strip the "is" prefix, causing a mismatch with what the client sends.
    public boolean getIsVolunteer() {
        return isVolunteer;
    }

    public void setIsVolunteer(boolean isVolunteer) {
        this.isVolunteer = isVolunteer;
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

    public List<Review> getSentReviews() {
        return sentReviews;
    }

    public void setSentReviews(List<Review> sentReviews) {
        this.sentReviews = sentReviews;
    }

    public List<Review> getReceivedReviews() {
        return receivedReviews;
    }

    public void setReceivedReviews(List<Review> receivedReviews) {
        this.receivedReviews = receivedReviews;
    }
}