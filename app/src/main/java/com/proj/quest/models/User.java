package com.proj.quest.models;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;
    @SerializedName("Логин")
    private String login;
    @SerializedName("Почта")
    private String email;
    @SerializedName("КоличествоБаллов")
    private int points;
    @SerializedName("ДатаРегистрации")
    private String registrationDate;

    public User() {}

    // Геттеры
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getEmail() { return email; }
    public int getPoints() { return points; }
    public String getRegistrationDate() { return registrationDate; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setEmail(String email) { this.email = email; }
    public void setPoints(int points) { this.points = points; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
