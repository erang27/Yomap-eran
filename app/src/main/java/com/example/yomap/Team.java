package com.example.yomap;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String title;
    private String id;
    private List<User> managerIds;
    private List<User> userIds;
    private List<Report> reports;

    public Team(String title) {
        this.title = title;
        userIds = new ArrayList<>();
        managerIds = new ArrayList<>();
        reports = new ArrayList<>();
    }
    public Team() {
        title = "my ken";
        managerIds = new ArrayList<>();
        managerIds = new ArrayList<>();
        reports = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<User> getManagers() {
        return managerIds;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public void setManagers(List<User> managers) {
        this.managerIds = managers;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {this.id = id;}
    public String getId() {return id;}
}
