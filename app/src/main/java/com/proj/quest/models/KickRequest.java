package com.proj.quest.models;

public class KickRequest {
    private int userId;
    public KickRequest(int userId) { this.userId = userId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
} 