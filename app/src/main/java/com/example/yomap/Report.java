package com.example.yomap;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Report {
    private String id;
    private String sender;
    private String issue;
    private String group;
    private int severity; //1 least severe, 10 most severe
    private int status; //0 unresolved, 1 in progress, 2 handled
    private com.google.firebase.Timestamp date;

    public Report(String sender, String issue, String group, int severity, int status) {
        this.sender = sender;
        this.issue = issue;
        this.group = group;
        this.severity = severity;
        this.status = status;
        this.date = com.google.firebase.Timestamp.now();
    }

    public Report() {
        sender = null;
        issue=null;
        group=null;
        id = null;
        severity = 5;
        status = 0;
        date = com.google.firebase.Timestamp.now();
    }


    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getSender() {
        return sender;
    }

    public String getIssue() {
        return issue;
    }

    public String getGroup() {
        return group;
    }

    public int getSeverity() {
        return severity;
    }

    public int getStatus() {
        return status;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public String toString() {

        return "sent by " + sender + " on " + UserSession.timeToString(date);
    }
}
