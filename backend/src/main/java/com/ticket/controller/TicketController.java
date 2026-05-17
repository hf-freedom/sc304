package com.ticket.controller;

import com.ticket.dto.*;
import com.ticket.entity.*;
import com.ticket.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/shows")
    public ApiResponse<List<Show>> getAllShows() {
        return seatService.getAllShows();
    }

    @GetMapping("/seats/{showId}")
    public ApiResponse<List<Seat>> getShowSeats(@PathVariable String showId) {
        return seatService.getShowSeats(showId);
    }

    @PostMapping("/lock")
    public ApiResponse<Order> lockSeats(@RequestBody LockSeatRequest request) {
        return seatService.lockSeats(request);
    }

    @PostMapping("/pay/{orderId}")
    public ApiResponse<Order> payOrder(@PathVariable String orderId, @RequestParam String userId) {
        return seatService.payOrder(orderId, userId);
    }

    @PostMapping("/refund/{orderId}")
    public ApiResponse<String> refundOrder(@PathVariable String orderId, @RequestParam String userId) {
        return seatService.refundOrder(orderId, userId);
    }

    @PostMapping("/recommend")
    public ApiResponse<List<Map<String, Object>>> recommendSeats(@RequestBody RecommendSeatsRequest request) {
        return seatService.recommendSeats(request);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<User> getUserInfo(@PathVariable String userId) {
        return seatService.getUserInfo(userId);
    }
}
