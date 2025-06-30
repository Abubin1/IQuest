package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Team implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("captainId")
    private int captainId;
    @SerializedName("members")
    private List<User> members;
    @SerializedName("eventId")
    private Integer eventId;

    public Team() {}

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCaptainId() { return captainId; }
    public void setCaptainId(int captainId) { this.captainId = captainId; }
    
    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    
    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}
