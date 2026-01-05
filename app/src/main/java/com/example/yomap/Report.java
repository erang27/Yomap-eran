package com.example.yomap;

import java.time.LocalDateTime;
import java.util.Date;

public class Report {
    private String sender;
    private String issue;
    private String group;
    private int severity; //1 least severe, 10 most severe
    private int status; //0 unresolved, 1 in progress, 2 handled
    private LocalDateTime date;

    public Report(String sender, String issue, String group, int severity, int status, LocalDateTime date) {
        this.sender = sender;
        this.issue = issue;
        this.group = group;
        this.severity = severity;
        this.status = status;
        this.date = date;
    }

    public Report() {
        sender = null;
        issue=null;
        group=null;
        severity = 5;
        status = 0;
        date = LocalDateTime.now();
    }

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

    public LocalDateTime getDate() {
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

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
