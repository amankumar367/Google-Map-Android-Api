package com.learning.aman.mapapi;

public class DetailsModel {

    private String time, distance;

    public DetailsModel() {
    }

    public DetailsModel(String time, String distance) {
        this.time = time;
        this.distance = distance;

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
