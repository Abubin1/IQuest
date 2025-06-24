package com.proj.quest.models;

import java.util.Date;

public class User {
    private int id;
    private String login;
    private String email;
    private int points;
    private Date registrationDate;

    public User() {}

    // Геттеры
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getEmail() { return email; }
    public int getPoints() { return points; }
    public Date getRegistrationDate() { return registrationDate; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setEmail(String email) { this.email = email; }
    public void setPoints(int points) { this.points = points; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }
}
