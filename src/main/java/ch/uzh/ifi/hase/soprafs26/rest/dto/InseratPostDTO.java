package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

public class InseratPostDTO {

    private String id;
    private String recipientId;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDate date;
    private String timeframe;
    private String workType;
    private String time;

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

    public String getWorkType() {return workType;}

    public void setWorkType(String workType) {this.workType = workType;}

    public String getTime(){return time;}

    public void setTime(String time){this.time = time;}
}