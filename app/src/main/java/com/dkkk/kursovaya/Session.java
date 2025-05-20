package com.dkkk.kursovaya;

public class Session {
    private int id;
    private String movieName;
    private String sessionDate;
    private String sessionTime;
    private int hallNumber;

    public Session(int id, String movieName, String sessionDate, String sessionTime, int hallNumber) {
        this.id = id;
        this.movieName = movieName;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.hallNumber = hallNumber;
    }

    public int getId() {
        return id;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public String getSessionTime() {
        return sessionTime;
    }

    public int getHallNumber() {
        return hallNumber;
    }
}
