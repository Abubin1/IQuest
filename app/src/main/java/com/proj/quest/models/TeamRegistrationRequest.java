package com.proj.quest.models;

import java.util.List;

public class TeamRegistrationRequest {
    private int teamId;
    private int eventId;
    private List<Integer> selectedMemberIds;

    public TeamRegistrationRequest() {
    }

    public TeamRegistrationRequest(int teamId, int eventId) {
        this.teamId = teamId;
        this.eventId = eventId;
    }

    public TeamRegistrationRequest(int teamId, int eventId, List<Integer> selectedMemberIds) {
        this.teamId = teamId;
        this.eventId = eventId;
        this.selectedMemberIds = selectedMemberIds;
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

    public List<Integer> getSelectedMemberIds() {
        return selectedMemberIds;
    }

    public void setSelectedMemberIds(List<Integer> selectedMemberIds) {
        this.selectedMemberIds = selectedMemberIds;
    }
} 