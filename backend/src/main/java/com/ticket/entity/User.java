package com.ticket.entity;

import com.ticket.enums.UserType;

public class User {
    private String id;
    private String name;
    private UserType type;
    private Boolean inBlacklist;

    public User(String id, String name, UserType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.inBlacklist = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }
    public Boolean getInBlacklist() { return inBlacklist; }
    public void setInBlacklist(Boolean inBlacklist) { this.inBlacklist = inBlacklist; }
}
