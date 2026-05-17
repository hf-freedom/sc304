package com.ticket.entity;

import com.ticket.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String showId;
    private List<String> seatIds;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;

    public Order(String id, String userId, String showId, List<String> seatIds, Double totalAmount) {
        this.id = id;
        this.userId = userId;
        this.showId = showId;
        this.seatIds = seatIds;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.createTime = LocalDateTime.now();
        this.expireTime = LocalDateTime.now().plusMinutes(5);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = seatIds; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
