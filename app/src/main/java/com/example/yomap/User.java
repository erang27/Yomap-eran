package com.example.yomap;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private ArrayList<String> teamIds;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        teamIds = new ArrayList<String>();
    }

    public User() {
        this.username = "";
        this.password = "";
        teamIds = new ArrayList<String>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getTeamsIds() {
        return teamIds;
    }

    public void setTeams(ArrayList<String> TeamIds) {
        this.teamIds = teamIds;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
