package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inserats")
public class Inserat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToMany
    @JoinTable(
        name = "inserat_volunteer_applied",
        joinColumns = @JoinColumn(name = "inserat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> volunteerApplied = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "volunteer_accepted_id")
    private User volunteerAccepted;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String timeframe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InseratStatus status = InseratStatus.OPEN;

    @Column(nullable = false)
    private LocalDate creationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(nullable = false)
    private String workType;

    @Column
    private String time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public List<User> getVolunteerApplied() {
        return volunteerApplied;
    }

    public void setVolunteerApplied(List<User> volunteerApplied) {
        this.volunteerApplied = volunteerApplied;
    }

    public User getVolunteerAccepted() {
        return volunteerAccepted;
    }

    public void setVolunteerAccepted(User volunteerAccepted) {
        this.volunteerAccepted = volunteerAccepted;
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

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getWorkType(){return workType;}

    public void setWorkType(String workType){this.workType = workType;}

    public String getTime(){return time;}

    public void setTime(String time){this.time = time;}
}