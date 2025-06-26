package com.proj.quest.models;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public class Event {
    private int id;
    @SerializedName("ДатаПроведения")
    private Date eventDate;
    private String startLocation;
    private int teamCount;
    private int maxTeamMembers;
    private int riddleCount;

    public Event() {}

    // Геттеры
    public int getId() { return id; }
    public Date getEventDate() { return eventDate; }
    public String getStartLocation() { return startLocation; }
    public int getTeamCount() { return teamCount; }
    public int getMaxTeamMembers() { return maxTeamMembers; }
    public int getRiddleCount() { return riddleCount; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setTeamCount(int teamCount) { this.teamCount = teamCount; }
    public void setMaxTeamMembers(int maxTeamMembers) { this.maxTeamMembers = maxTeamMembers; }
    public void setRiddleCount(int riddleCount) { this.riddleCount = riddleCount; }
}
