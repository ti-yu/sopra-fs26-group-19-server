package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.ReviewStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "inserat_id", nullable = false)
    private Inserat inserat;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private boolean finished;

    @Column(nullable = false, length = 100)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus;

    /**
     * 1.0 - 5.0 in 0.5 steps. Nullable so existing rows (created before the
     * feature shipped) don't need a back-fill.
     */
    @Column(nullable = true)
    private Double stars;

    /**
     * When the user "ignores for now", we set this timestamp to now + 24 h.
     * The review popup endpoint skips reviews while ignoreUntil is still in
     * the future, so the popup re-appears one day later. Nullable.
     */
    @Column(nullable = true)
    private LocalDateTime ignoreUntil;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Inserat getInserat() {
        return inserat;
    }

    public void setInserat(Inserat inserat) {
        this.inserat = inserat;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Double getStars() {
        return stars;
    }

    public void setStars(Double stars) {
        this.stars = stars;
    }

    public LocalDateTime getIgnoreUntil() {
        return ignoreUntil;
    }

    public void setIgnoreUntil(LocalDateTime ignoreUntil) {
        this.ignoreUntil = ignoreUntil;
    }
}