package com.proj.quest.models;

public class TeamRegistrationRequest {
    private int teamId;
    private int eventId;

    public TeamRegistrationRequest() {
    }

    public TeamRegistrationRequest(int teamId, int eventId) {
        this.teamId = teamId;
        this.eventId = eventId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
} 