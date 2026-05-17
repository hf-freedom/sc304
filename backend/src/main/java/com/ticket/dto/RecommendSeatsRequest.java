package com.ticket.dto;

public class RecommendSeatsRequest {
    private String showId;
    private Integer seatCount;

    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
}
