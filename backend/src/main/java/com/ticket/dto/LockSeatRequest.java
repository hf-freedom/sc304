package com.ticket.dto;

import java.util.List;

public class LockSeatRequest {
    private String showId;
    private String userId;
    private List<String> seatIds;

    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = seatIds; }
}
