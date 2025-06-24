package com.proj.quest.models;

public class Landmark {
    private int id;
    private String name;
    private String street;
    private double latitude;
    private double longitude;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
