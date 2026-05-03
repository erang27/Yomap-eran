package com.example.yomap;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String title;
    private String id;
    private String founder;
    private List<String> managers;
    private List<String> users;
    private List<String> pendingUsers;
    private List<Report> reports;

    public Team(String title) {
        this.title = title;
        this.founder = "";
        users = new ArrayList<>();
        pendingUsers = new ArrayList<>();
        managers = new ArrayList<>();
        reports = new ArrayList<>();
    }
    public Team() {
        title = "my ken";
        this.founder ="";
        managers = new ArrayList<>();
        users = new ArrayList<>();
        pendingUsers = new ArrayList<>();
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
    public void addPendingUsers(String id) {if (!isMember(id)) pendingUsers.add(id);}
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("pendingMembers")
    public List<String> getPendingUsers() {
        if (pendingUsers == null) pendingUsers = new ArrayList<>();
        return pendingUsers;
    }

    @PropertyName("pendingMembers")
    public void setPendingUsers(List<String> pendingUsers) {this.pendingUsers = pendingUsers;}

    @PropertyName("users")
    public List<String> getMembers() {
        if (users == null) users = new ArrayList<>();
        return users;
    }
    @PropertyName("users")
    public void setMembers(List<String> users) { this.users = users; }

    public String getFounder() {return founder;}

    public void setFounder(String founder) {this.founder = founder;}

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

    //removes a user from the team
    public void removeMember(String exmember) {
        boolean found = false;
        for (int i = 0; i < users.size() && !found; i++) {
            if (exmember.equals(users.get(i))) {
                users.remove(i);
                found = true;
            }
        }
    }

    //the method handles both accepting and rejecting members
    public void unpendMember(String member, boolean gotin) {
        if (gotin) {
            addUsers(member);
        }
        boolean found = false;
        for (int i = 0; i < pendingUsers.size() && !found; i++) {
            if (member.equals(pendingUsers.get(i))) {
                pendingUsers.remove(i);
                found = true;
            }
        }
    }

    //demotes a manager to a regular member
    public void demoteManager(String exmanager) {
        boolean found = false;
        for (int i = 0; i < managers.size() && !found; i++) {
            if (exmanager.equals(managers.get(i))) {
                managers.remove(i);
                found = true;
            }
        }
    }

    //checks if user if the founder of the team
    public boolean isFounder(String username) {
        if (username==null) return false;
        return username.equals(founder);
    }

    @Override
    public String toString() {
        return title;
    }
}
