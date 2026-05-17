package com.ticket.storage;

import com.ticket.entity.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    public static final Map<String, Show> SHOWS = new ConcurrentHashMap<>();
    public static final Map<String, User> USERS = new ConcurrentHashMap<>();
    public static final Map<String, Order> ORDERS = new ConcurrentHashMap<>();
    public static final Map<String, UserLockRecord> USER_LOCK_RECORDS = new ConcurrentHashMap<>();

    static {
        initData();
    }

    private static void initData() {
        Show show1 = new Show("show001", "演唱会-周杰伦", LocalDateTime.now().plusDays(7), 8, 12);
        Show show2 = new Show("show002", "话剧-雷雨", LocalDateTime.now().plusDays(3), 6, 10);

        initSeats(show1, 100.0, 150.0, 200.0);
        initSeats(show2, 80.0, 120.0, 180.0);

        SHOWS.put(show1.getId(), show1);
        SHOWS.put(show2.getId(), show2);

        USERS.put("user001", new User("user001", "张三", com.ticket.enums.UserType.MEMBER));
        USERS.put("user002", new User("user002", "李四", com.ticket.enums.UserType.NORMAL));
        USERS.put("user003", new User("user003", "王五", com.ticket.enums.UserType.NORMAL));
    }

    private static void initSeats(Show show, Double lowPrice, Double midPrice, Double highPrice) {
        int midRow = show.getTotalRows() / 2;
        for (int row = 1; row <= show.getTotalRows(); row++) {
            for (int col = 1; col <= show.getTotalColumns(); col++) {
                String seatId = show.getId() + "_" + row + "_" + col;
                Double price;
                if (row <= midRow - 1) {
                    price = highPrice;
                } else if (row <= midRow + 1) {
                    price = midPrice;
                } else {
                    price = lowPrice;
                }
                Seat seat = new Seat(seatId, show.getId(), row, col, price);
                show.getSeats().add(seat);
            }
        }
    }
}
