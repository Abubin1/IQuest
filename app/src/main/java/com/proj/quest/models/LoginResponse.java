package com.proj.quest.models;

public class LoginResponse {
    private String token;
    private User user;

    // Геттеры и сеттеры
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
