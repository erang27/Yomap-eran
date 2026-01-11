package com.example.yomap;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private ArrayList<String> teamIds;
    private String Token;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
        teamIds = new ArrayList<String>();
        Token = "";
    }

    public User() {
        this.username = "";
        this.password = "";
        teamIds = new ArrayList<String>();
        Token = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(ArrayList<String> teamIds) {
        this.teamIds = teamIds;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
