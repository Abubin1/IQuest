package com.proj.quest.models;

import java.io.Serializable;
import java.util.Date;
import com.google.gson.annotations.SerializedName;

public class Event implements Serializable {
    private int id;
    @SerializedName("Название")
    private String name;
    @SerializedName("ДатаПроведения")
    private Date eventDate;
    @SerializedName("ДатаНачала")
    private String startDate;
    @SerializedName("ВремяНачала")
    private String startTime;
    @SerializedName("МестоСтарта")
    private String startLocation;
    @SerializedName("КоличествоКоманд")
    private int teamCount;
    @SerializedName("МаксимальноеКоличествоЧеловекВК")
    private int maxTeamMembers;
    @SerializedName("КоличествоЗагадок")
    private int riddleCount;
    @SerializedName("Организатор")
    private String organizer;
    @SerializedName("Описание")
    private String description;

    public Event() {}

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public Date getEventDate() { return eventDate; }
    public String getStartDate() { return startDate; }
    public String getStartTime() { return startTime; }
    public String getStartLocation() { return startLocation; }
    public int getTeamCount() { return teamCount; }
    public int getMaxTeamMembers() { return maxTeamMembers; }
    public int getRiddleCount() { return riddleCount; }
    public String getOrganizer() { return organizer; }
    public String getDescription() { return description; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setTeamCount(int teamCount) { this.teamCount = teamCount; }
    public void setMaxTeamMembers(int maxTeamMembers) { this.maxTeamMembers = maxTeamMembers; }
    public void setRiddleCount(int riddleCount) { this.riddleCount = riddleCount; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public void setDescription(String description) { this.description = description; }
}
