package com.example.yomap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class Register extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText inputUsername, inputPassword, inputCPassword;
    Button register, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initializing all layout components
        inputUsername = findViewById(R.id.newUsername);
        inputPassword = findViewById(R.id.newPassword);
        inputCPassword = findViewById(R.id.confirmPassword);
        login = findViewById(R.id.buttonLogin);
        register = findViewById(R.id.buttonRegister);

        //register button
        register.setOnClickListener(v -> createNewAccount());

        //already have account button- back to login screen
        login.setOnClickListener(v -> finish());
    }




    //clears all fields
    private void clearAllFields() {
        inputCPassword.setText("");
        inputPassword.setText("");
        inputUsername.setText("");
    }


    private void createNewAccount() {
        String username, password, cpassword;
        username = inputUsername.getText().toString();
        password = inputPassword.getText().toString();
        cpassword = inputCPassword.getText().toString();

        //check if all fields are full
        if (username.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
            Toast.makeText(Register.this, "Some or all fields are empty", Toast.LENGTH_SHORT).show();
        }
        //check if password is written correctly the second time
        else if (!password.equals(inputCPassword.getText().toString())) {
            Toast.makeText(Register.this, "'password' and 'confirm passowrd' must contain the same input", Toast.LENGTH_SHORT).show();
            inputCPassword.setText("");
            inputPassword.setText("");
        }
        else {
            db.collection("Users").document(username).get()
                .addOnSuccessListener(docRef -> {
                    if (docRef.exists()) {
                        Toast.makeText(this, "Username taken", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        User newuser = new User(username, password);
                        db.collection("Users").document(username).set(newuser)
                                .addOnSuccessListener(docRef1 -> {
                                    clearAllFields();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("erroraddfirebase", "error adding user", e);
                                });
                    }})
                    .addOnFailureListener(e -> {
                        Log.w("erroraddfirebase", "error adding user", e);
                    });
                }

        }

}




