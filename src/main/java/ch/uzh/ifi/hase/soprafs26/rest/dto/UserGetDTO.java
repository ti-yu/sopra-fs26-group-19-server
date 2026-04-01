package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.time.LocalDate;

public class UserGetDTO {

	private String id;
	private String name;
	private String username;
	private String bio;
	private LocalDate dateOfBirth;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return bio;
	}

	public void setBio (String bio) {
		this.bio = bio;
	}

	public LocalDate getDateOfBirth () {
		return dateOfBirth;
	}

	public void setDateOfBirth (LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
}
