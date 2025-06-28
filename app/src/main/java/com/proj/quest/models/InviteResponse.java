package com.proj.quest.models;

import com.google.gson.annotations.SerializedName;

public class InviteResponse {
    private int id;
    @SerializedName("Команда")
    private int teamId;
    @SerializedName("Пользователь")
    private int userId;
    @SerializedName("team_name")
    private String teamName;
    @SerializedName("event_id")
    private int eventId;
    @SerializedName("captain_login")
    private String captainLogin;
    @SerializedName("Статус")
    private String status;
    @SerializedName("Дата")
    private String date;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getCaptainLogin() { return captainLogin; }
    public void setCaptainLogin(String captainLogin) { this.captainLogin = captainLogin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
} 