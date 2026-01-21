package com.example.yomap;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserSession {
    private static String username;
    public UserSession(String username) {
        this.username = username;
    }
    public static String getUsername() {return username;}

    public static String timeToString(Timestamp ts)  {
        LocalDateTime dateTime =
                Instant.ofEpochSecond(ts.getSeconds(), ts.getNanoseconds())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

}
