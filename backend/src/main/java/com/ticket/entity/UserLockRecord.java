package com.ticket.entity;

import java.util.LinkedList;
import java.util.Queue;

public class UserLockRecord {
    private String userId;
    private Queue<Long> lockTimes;
    private Integer lockCount;

    public UserLockRecord(String userId) {
        this.userId = userId;
        this.lockTimes = new LinkedList<>();
        this.lockCount = 0;
    }

    public void addLockRecord(Long time) {
        lockTimes.add(time);
        lockCount++;
    }

    public void removeExpiredRecords(Long beforeTime) {
        while (!lockTimes.isEmpty() && lockTimes.peek() < beforeTime) {
            lockTimes.poll();
            lockCount--;
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Queue<Long> getLockTimes() { return lockTimes; }
    public void setLockTimes(Queue<Long> lockTimes) { this.lockTimes = lockTimes; }
    public Integer getLockCount() { return lockCount; }
    public void setLockCount(Integer lockCount) { this.lockCount = lockCount; }
}
