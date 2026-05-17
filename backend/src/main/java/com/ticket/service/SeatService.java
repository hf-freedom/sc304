package com.ticket.service;

import com.ticket.dto.*;
import com.ticket.entity.*;
import com.ticket.enums.*;
import com.ticket.storage.DataStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    @Value("${seat.lock.timeout:300}")
    private Integer lockTimeout;

    @Value("${show.nearest.time:30}")
    private Integer nearestShowTime;

    @Value("${lock.frequency.threshold:10}")
    private Integer frequencyThreshold;

    @Value("${lock.frequency.window:60}")
    private Integer frequencyWindow;

    public ApiResponse<Order> lockSeats(LockSeatRequest request) {
        User user = DataStorage.USERS.get(request.getUserId());
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        if (user.getInBlacklist()) {
            return ApiResponse.error("您因异常操作已被限制购票");
        }

        if (!checkLockFrequency(request.getUserId())) {
            user.setInBlacklist(true);
            return ApiResponse.error("锁座频率过高，您已被限制购票");
        }

        Show show = DataStorage.SHOWS.get(request.getShowId());
        if (show == null) {
            return ApiResponse.error("场次不存在");
        }

        if (show.getStatus() == ShowStatus.MEMBER_PRE_SALE && user.getType() == UserType.NORMAL) {
            return ApiResponse.error("当前为会员优先购阶段，普通用户暂不可购票");
        }

        if (show.getStatus() == ShowStatus.STARTED || show.getStatus() == ShowStatus.ENDED) {
            return ApiResponse.error("该场次已不可购票");
        }

        List<Seat> seats = show.getSeats().stream()
                .filter(s -> request.getSeatIds().contains(s.getId()))
                .collect(Collectors.toList());

        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.LOCKED) {
                String lockerInfo = "";
                if (seat.getLockUserId() != null) {
                    User locker = DataStorage.USERS.get(seat.getLockUserId());
                    if (locker != null) {
                        lockerInfo = "，当前由用户【" + locker.getName() + "】锁定中";
                    }
                }
                long remainTime = 0;
                if (seat.getLockTime() != null) {
                    remainTime = (lockTimeout * 1000 - (System.currentTimeMillis() - seat.getLockTime())) / 1000;
                    if (remainTime > 0) {
                        lockerInfo += "，预计 " + remainTime + " 秒后释放";
                    }
                }
                return ApiResponse.error("座位 " + seat.getRow() + "排" + seat.getColumn() + "座 已被锁定" + lockerInfo + "，请选择其他座位或稍后再试");
            } else if (seat.getStatus() == SeatStatus.SOLD) {
                return ApiResponse.error("座位 " + seat.getRow() + "排" + seat.getColumn() + "座 已售出，请选择其他座位");
            } else if (seat.getStatus() != SeatStatus.AVAILABLE) {
                return ApiResponse.error("座位 " + seat.getRow() + "排" + seat.getColumn() + "座 暂不可选，请选择其他座位");
            }
        }

        String orderId = "ORD" + System.currentTimeMillis();
        Double totalAmount = seats.stream().mapToDouble(Seat::getPrice).sum();
        Order order = new Order(orderId, request.getUserId(), request.getShowId(), request.getSeatIds(), totalAmount);

        Long currentTime = System.currentTimeMillis();
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockTime(currentTime);
            seat.setLockUserId(request.getUserId());
            seat.setOrderId(orderId);
        }

        DataStorage.ORDERS.put(orderId, order);
        recordLockFrequency(request.getUserId(), currentTime);

        return ApiResponse.success("锁座成功", order);
    }

    public ApiResponse<Order> payOrder(String orderId, String userId) {
        Order order = DataStorage.ORDERS.get(orderId);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            return ApiResponse.error("无权操作此订单");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            return ApiResponse.error("订单状态不正确");
        }

        if (LocalDateTime.now().isAfter(order.getExpireTime())) {
            releaseSeats(order);
            order.setStatus(OrderStatus.CANCELLED);
            return ApiResponse.error("订单已超时，座位已释放");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());

        Show show = DataStorage.SHOWS.get(order.getShowId());
        for (Seat seat : show.getSeats()) {
            if (order.getSeatIds().contains(seat.getId())) {
                seat.setStatus(SeatStatus.SOLD);
            }
        }

        return ApiResponse.success("支付成功", order);
    }

    public ApiResponse<String> refundOrder(String orderId, String userId) {
        Order order = DataStorage.ORDERS.get(orderId);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            return ApiResponse.error("无权操作此订单");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            return ApiResponse.error("只有已支付订单可退票");
        }

        Show show = DataStorage.SHOWS.get(order.getShowId());
        long minutesToShow = ChronoUnit.MINUTES.between(LocalDateTime.now(), show.getStartTime());
        if (minutesToShow < nearestShowTime) {
            return ApiResponse.error("距开场不足" + nearestShowTime + "分钟，不可退票");
        }

        order.setStatus(OrderStatus.REFUNDED);

        for (Seat seat : show.getSeats()) {
            if (order.getSeatIds().contains(seat.getId())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockTime(null);
                seat.setLockUserId(null);
                seat.setOrderId(null);
            }
        }

        return ApiResponse.success("退票成功", "退款金额: " + order.getTotalAmount());
    }

    public ApiResponse<List<Map<String, Object>>> recommendSeats(RecommendSeatsRequest request) {
        Show show = DataStorage.SHOWS.get(request.getShowId());
        if (show == null) {
            return ApiResponse.error("场次不存在");
        }

        int seatCount = request.getSeatCount();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<Integer, List<Seat>> seatsByRow = show.getSeats().stream()
                .collect(Collectors.groupingBy(Seat::getRow));

        int midRow = show.getTotalRows() / 2 + 1;
        int midCol = show.getTotalColumns() / 2 + 1;

        for (int row = 1; row <= show.getTotalRows(); row++) {
            List<Seat> rowSeats = seatsByRow.get(row);
            if (rowSeats == null) continue;

            rowSeats.sort(Comparator.comparingInt(Seat::getColumn));

            for (int i = 0; i <= rowSeats.size() - seatCount; i++) {
                boolean allAvailable = true;
                List<Seat> candidate = new ArrayList<>();

                for (int j = 0; j < seatCount; j++) {
                    Seat seat = rowSeats.get(i + j);
                    if (seat.getStatus() != SeatStatus.AVAILABLE) {
                        allAvailable = false;
                        break;
                    }
                    candidate.add(seat);
                }

                if (allAvailable) {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("seats", candidate);
                    rec.put("row", row);
                    
                    double totalPrice = candidate.stream().mapToDouble(Seat::getPrice).sum();
                    rec.put("totalPrice", totalPrice);
                    
                    int avgCol = candidate.stream().mapToInt(Seat::getColumn).sum() / candidate.size();
                    int rowDistance = Math.abs(row - midRow);
                    int colDistance = Math.abs(avgCol - midCol);
                    int score = rowDistance * 10 + colDistance;
                    rec.put("score", score);
                    
                    String quality;
                    if (rowDistance <= 1 && colDistance <= 2) {
                        quality = "gold";
                    } else if (rowDistance <= 2 && colDistance <= 3) {
                        quality = "silver";
                    } else {
                        quality = "bronze";
                    }
                    rec.put("quality", quality);
                    
                    recommendations.add(rec);
                }
            }
        }

        recommendations.sort(Comparator.comparingInt(r -> (int) r.get("score")));

        List<Map<String, Object>> result = recommendations.stream()
                .limit(8)
                .collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    public void releaseExpiredLocks() {
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime - (lockTimeout * 1000);

        for (Show show : DataStorage.SHOWS.values()) {
            for (Seat seat : show.getSeats()) {
                if (seat.getStatus() == SeatStatus.LOCKED && seat.getLockTime() != null && seat.getLockTime() < expireTime) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setLockTime(null);
                    seat.setLockUserId(null);

                    if (seat.getOrderId() != null) {
                        Order order = DataStorage.ORDERS.get(seat.getOrderId());
                        if (order != null && order.getStatus() == OrderStatus.PENDING) {
                            order.setStatus(OrderStatus.CANCELLED);
                        }
                    }
                    seat.setOrderId(null);
                }
            }
        }
    }

    public void updateShowStatus() {
        LocalDateTime now = LocalDateTime.now();
        for (Show show : DataStorage.SHOWS.values()) {
            if (show.getMemberPreSaleTime() != null && now.isAfter(show.getMemberPreSaleTime())
                    && (show.getPublicSaleTime() == null || now.isBefore(show.getPublicSaleTime()))) {
                show.setStatus(ShowStatus.MEMBER_PRE_SALE);
            } else if (now.isBefore(show.getStartTime().minusMinutes(nearestShowTime))) {
                show.setStatus(ShowStatus.ON_SALE);
            } else if (now.isBefore(show.getStartTime())) {
                show.setStatus(ShowStatus.NEAR_START);
            } else if (now.isBefore(show.getEndTime() != null ? show.getEndTime() : show.getStartTime().plusHours(2))) {
                show.setStatus(ShowStatus.STARTED);
            } else {
                show.setStatus(ShowStatus.ENDED);
            }
        }
    }

    private void releaseSeats(Order order) {
        Show show = DataStorage.SHOWS.get(order.getShowId());
        for (Seat seat : show.getSeats()) {
            if (order.getSeatIds().contains(seat.getId())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockTime(null);
                seat.setLockUserId(null);
                seat.setOrderId(null);
            }
        }
    }

    private boolean checkLockFrequency(String userId) {
        UserLockRecord record = DataStorage.USER_LOCK_RECORDS.get(userId);
        if (record == null) {
            return true;
        }

        long windowStart = System.currentTimeMillis() - (frequencyWindow * 1000);
        record.removeExpiredRecords(windowStart);

        return record.getLockCount() < frequencyThreshold;
    }

    private void recordLockFrequency(String userId, Long time) {
        UserLockRecord record = DataStorage.USER_LOCK_RECORDS.computeIfAbsent(userId, UserLockRecord::new);
        record.addLockRecord(time);
    }

    public ApiResponse<List<Show>> getAllShows() {
        return ApiResponse.success(new ArrayList<>(DataStorage.SHOWS.values()));
    }

    public ApiResponse<List<Seat>> getShowSeats(String showId) {
        Show show = DataStorage.SHOWS.get(showId);
        if (show == null) {
            return ApiResponse.error("场次不存在");
        }
        return ApiResponse.success(show.getSeats());
    }

    public ApiResponse<User> getUserInfo(String userId) {
        User user = DataStorage.USERS.get(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }
}
