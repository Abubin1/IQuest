package com.proj.quest.models;

import java.util.List;

public class Team {
    private int id;
    private String name;
    private int captainId;
    private int eventId;
    private List<Integer> members;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCaptainId() { return captainId; }
    public void setCaptainId(int captainId) { this.captainId = captainId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public List<Integer> getMembers() { return members; }
    public void setMembers(List<Integer> members) { this.members = members; }
}
