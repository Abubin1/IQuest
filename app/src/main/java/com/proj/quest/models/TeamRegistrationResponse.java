package com.proj.quest.models;

public class TeamRegistrationResponse {
    private boolean success;
    private String message;

    public TeamRegistrationResponse() {
    }

    public TeamRegistrationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 