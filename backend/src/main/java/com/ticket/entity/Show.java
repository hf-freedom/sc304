package com.ticket.entity;

import com.ticket.enums.ShowStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Show {
    private String id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime memberPreSaleTime;
    private LocalDateTime publicSaleTime;
    private ShowStatus status;
    private Integer totalRows;
    private Integer totalColumns;
    private List<Seat> seats;

    public Show(String id, String name, LocalDateTime startTime, Integer totalRows, Integer totalColumns) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;
        this.seats = new ArrayList<>();
        this.status = ShowStatus.ON_SALE;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getMemberPreSaleTime() { return memberPreSaleTime; }
    public void setMemberPreSaleTime(LocalDateTime memberPreSaleTime) { this.memberPreSaleTime = memberPreSaleTime; }
    public LocalDateTime getPublicSaleTime() { return publicSaleTime; }
    public void setPublicSaleTime(LocalDateTime publicSaleTime) { this.publicSaleTime = publicSaleTime; }
    public ShowStatus getStatus() { return status; }
    public void setStatus(ShowStatus status) { this.status = status; }
    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }
    public Integer getTotalColumns() { return totalColumns; }
    public void setTotalColumns(Integer totalColumns) { this.totalColumns = totalColumns; }
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}
