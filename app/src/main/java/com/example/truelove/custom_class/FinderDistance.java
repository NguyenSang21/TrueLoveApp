package com.example.truelove.custom_class;

public class FinderDistance {
    private User user;
    private float distance;
    // don vi meter or km
    private String unit;

    public FinderDistance() {
        super();
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public FinderDistance(User user, float distance, String unit) {
        this.user = user;
        this.distance = distance;
        this.unit=unit;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public User getUser() {
        return user;
    }

    public float getDistance() {
        return distance;
    }
}
