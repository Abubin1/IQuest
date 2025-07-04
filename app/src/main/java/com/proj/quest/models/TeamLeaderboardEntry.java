package com.proj.quest.models;

public class TeamLeaderboardEntry {
    private int id;
    private String name;
    private int captainId;
    private int score;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCaptainId() { return captainId; }
    public void setCaptainId(int captainId) { this.captainId = captainId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
} 