package com.ticket.task;

import com.ticket.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private SeatService seatService;

    @Scheduled(fixedRate = 10000)
    public void releaseExpiredLocks() {
        seatService.releaseExpiredLocks();
    }

    @Scheduled(fixedRate = 60000)
    public void updateShowStatus() {
        seatService.updateShowStatus();
    }
}
