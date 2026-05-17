package com.ticket.entity;

import com.ticket.enums.SeatStatus;

public class Seat {
    private String id;
    private String showId;
    private Integer row;
    private Integer column;
    private Double price;
    private SeatStatus status;
    private Long lockTime;
    private String lockUserId;
    private String orderId;

    public Seat(String id, String showId, Integer row, Integer column, Double price) {
        this.id = id;
        this.showId = showId;
        this.row = row;
        this.column = column;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }
    public Integer getColumn() { return column; }
    public void setColumn(Integer column) { this.column = column; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public Long getLockTime() { return lockTime; }
    public void setLockTime(Long lockTime) { this.lockTime = lockTime; }
    public String getLockUserId() { return lockUserId; }
    public void setLockUserId(String lockUserId) { this.lockUserId = lockUserId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
