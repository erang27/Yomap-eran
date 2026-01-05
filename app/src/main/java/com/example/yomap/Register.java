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
import com.google.firebase.firestore.FirebaseFirestore;

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

    //create new user account and upload to firebase
    private void createNewAccount() {
        String username, password;
        username = inputUsername.getText().toString();
        password = inputPassword.getText().toString();
        if (!password.equals(inputCPassword.getText().toString())) {
            Toast.makeText(Register.this, "'password' and 'confirm passowrd' must contain the same input", Toast.LENGTH_SHORT).show();
            clearAllFields();
        }
        else {
            User newuser = new User(username, password);
            db.collection("Users").document(username).set(newuser)
                    .addOnSuccessListener(docRef -> {
                        clearAllFields();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding user", e));
        }
    }

    //clears all fields
    private void clearAllFields() {
        inputCPassword.setText("");
        inputPassword.setText("");
        inputUsername.setText("");
    }




}



