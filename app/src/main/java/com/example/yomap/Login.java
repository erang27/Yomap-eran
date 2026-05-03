package com.example.yomap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText inputUsername, inputPassword;
    Button login, register;
    User newuser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inputUsername = findViewById(R.id.Username);
        inputPassword = findViewById(R.id.Password);
        login = findViewById(R.id.buttonLogin);
        register = findViewById(R.id.buttonRegister);

        //login button
        login.setOnClickListener(v-> correctPassword());
        //new account button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                activityResultLauncher.launch(intent);
            }
        });
    }

    private void correctPassword() {
        String username, password;
        username = inputUsername.getText().toString();
        password = inputPassword.getText().toString();
        if (username.isBlank()) {
            Toast.makeText(Login.this, "no username was inputted", Toast.LENGTH_SHORT).show();
        }
        else if (password.isBlank()) {
            Toast.makeText(Login.this, "no password was inputted", Toast.LENGTH_SHORT).show();
        }

        else {
            db.collection("Users").document(username).get()
                    .addOnSuccessListener(docRef -> {
                        if (!docRef.exists()) {
                            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                        else {newuser = docRef.toObject(User.class);

                        if (newuser!=null && newuser.getPassword().equals(password)){
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        UserSession us = new UserSession(inputUsername.getText().toString());
                        activityResultLauncher.launch(intent);
                        finish(); }
                        else {
                            Toast.makeText(this, "Username and password dont match", Toast.LENGTH_SHORT).show();
                        } }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("fail_to_check_password", "fail to check password", e);
                    });
        }
    }



    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                }
            }
    );
}