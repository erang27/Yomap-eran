package com.example.yomap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListView listView;
    ArrayAdapter<Team> adapter;
    ArrayList<Team> teams = new ArrayList<>();
    Button add, logoutbtn;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initializing components
        listView = findViewById(R.id.list);
        add =findViewById(R.id.newActivity);
        logoutbtn = findViewById(R.id.logOut);
        username = UserSession.getUsername();

        //loading firebase teamlist
        db.collection("Users").document(username).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    teams.clear();
                    if (user != null && user.getTeamIds() != null && !user.getTeamIds().isEmpty()) {
                    db.collection("Teams")
                            .whereIn(FieldPath.documentId(), user.getTeamIds())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                for (DocumentSnapshot d : snapshot) {
                                    Team t = d.toObject(Team.class);
                                    if (t!=null) {t.setId(d.getId());
                                    teams.add(t);}
                                }
                                adapter = new ArrayAdapter<Team>(this, android.R.layout.simple_list_item_1, teams) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        return view;
                                    }
                                };
                                listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }); }
                    else {
                        Toast.makeText(MainActivity.this, "no teams found", Toast.LENGTH_SHORT).show();
                    }
                });


        add.setOnClickListener(v-> addTeam());
        logoutbtn.setOnClickListener(v-> logOut());
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, TeamActivity.class);
            intent.putExtra("teamId", teams.get(position).getId());
            activityResultLauncher.launch(intent);
        });


    }




    //opens a new team and adds it to firebase and listview
    //todo: limit each user to 10 teams
    private void addTeam() {
        EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Add new team")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    Team newTeam = new Team(input.getText().toString());
                    newTeam.addManagers(username); //set the opener of the team as the manager
                    newTeam.addUsers(username);
                    newTeam.setFounder(username);
                    db.collection("Teams").
                            add(newTeam)
                            .addOnSuccessListener(docRef -> {
                                db.collection("Users").document(username)
                                                .update("teamIds", FieldValue.arrayUnion(docRef.getId()))
                                                        .addOnSuccessListener(docRef1 -> {
                                                            newTeam.setId(docRef.getId());
                                                            teams.add(newTeam);
                                                            adapter.notifyDataSetChanged();
                                                        });
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    //deletes all other activities and opens Login
    private void logOut() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityResultLauncher.launch(intent);
        finish();
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