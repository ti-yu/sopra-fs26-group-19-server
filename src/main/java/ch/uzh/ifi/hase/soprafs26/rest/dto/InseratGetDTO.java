package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import java.time.LocalDate;

public class InseratGetDTO {

    private String id;
    private String recipientId;
    private String recipientUsername;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public InseratStatus getStatus() {
        return status;
    }

    public void setStatus(InseratStatus status) {
        this.status = status;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public int getVolunteerAppliedCount() {
        return volunteerAppliedCount;
    }

    public void setVolunteerAppliedCount(int volunteerAppliedCount) {
        this.volunteerAppliedCount = volunteerAppliedCount;
    }

    public String getVolunteerAcceptedUsername() {
        return volunteerAcceptedUsername;
    }

    public void setVolunteerAcceptedUsername(String volunteerAcceptedUsername) {
        this.volunteerAcceptedUsername = volunteerAcceptedUsername;
    }

    public String getVolunteerAcceptedPhone() {
        return volunteerAcceptedPhone;
    }

    public void setVolunteerAcceptedPhone(String volunteerAcceptedPhone) {
        this.volunteerAcceptedPhone = volunteerAcceptedPhone;
    }

    public String getVolunteerAcceptedEmail() {
        return volunteerAcceptedEmail;
    }

    public void setVolunteerAcceptedEmail(String volunteerAcceptedEmail) {
        this.volunteerAcceptedEmail = volunteerAcceptedEmail;
    }
}