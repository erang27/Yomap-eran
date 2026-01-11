package com.example.yomap;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String title;
    private String id;
    private List<String> managers;
    private List<String> users;
    private List<Report> reports;

    public Team(String title) {
        this.title = title;
        users = new ArrayList<>();
        managers = new ArrayList<>();
        reports = new ArrayList<>();
    }
    public Team() {
        title = "my ken";
        managers = new ArrayList<>();
        users = new ArrayList<>();
        reports = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getManagers() {
        if (managers == null) managers = new ArrayList<>();
        return managers;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public void setManagers(List<String> managers) {
        this.managers = managers;
    }
    public void addManagers(String id) {managers.add(id);}
    public void addUsers(String id) {users.add(id);}
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("users")
    public List<String> getMembers() {
        if (users == null) users = new ArrayList<>();
        return users;
    }
    @PropertyName("users")
    public void setMembers(List<String> users) { this.users = users; }

    public void setId(String id) {this.id = id;}
    public String getId() {return id;}

    //checks for a user whether it's a manager of this team
    public boolean isManager(String username) {
        for (int i = 0; i< managers.size(); i++) {
            if (username.equals(managers.get(i))) return true;
        }
        return false;
    }

    //checks whether a user is a member of this team
    public boolean isMember(String username) {
        for (int i = 0; i< users.size(); i++) {
            if (username.equals(users.get(i))) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return title;
    }
}
