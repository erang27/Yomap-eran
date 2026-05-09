package com.example.yomap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
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
    Button addButton, logoutButton, requestButton;
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
        addButton =findViewById(R.id.newActivity);
        logoutButton = findViewById(R.id.logOut);
        requestButton = findViewById(R.id.requestJoin);
        username = UserSession.getUsername();

        //buttons
        addButton.setOnClickListener(v-> addTeam());
        logoutButton.setOnClickListener(v-> logOut());
        requestButton.setOnClickListener(v-> requestJoin());
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, TeamActivity.class);
            intent.putExtra("teamId", teams.get(position).getId());
            activityResultLauncher.launch(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        username = UserSession.getUsername();
        loadteams();
    }

    //loads all of the teams, up to date with firebase data
    private void loadteams() {
        db.collection("Users").document(username).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null && user.getTeamIds() != null && !user.getTeamIds().isEmpty()) {
                        db.collection("Teams")
                                .whereIn(FieldPath.documentId(), user.getTeamIds())
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    teams.clear();
                                    for (DocumentSnapshot d : snapshot) {
                                        Team t = d.toObject(Team.class);
                                        if (t != null && d.getId() != null) {
                                            t.setId(d.getId());
                                            teams.add(t);
                                        }
                                    }
                                    if (adapter == null) {
                                        adapter = new ArrayAdapter<>(this,
                                                android.R.layout.simple_list_item_1, teams);
                                        listView.setAdapter(adapter);
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    } else {
                        teams.clear();
                        if (adapter == null) {
                            adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, teams);
                            listView.setAdapter(adapter);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    //opens a new team and adds it to firebase and listview
    private void addTeam() {
        if (teams.size()>9) {
            Toast.makeText(this, "Teams are limited up to 10", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText input = new EditText(this);
        input.setHint("New Team Name");
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

    //opens a dialog where the user types the desired team ID
    private void requestJoin() {
        EditText input = new EditText(this);
        input.setHint("Enter desired team ID");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Request To Join A Team")
                .setView(input)
                .setPositiveButton("Send Request", (d,ok)-> sendJoinRequest(input.getText().toString().trim()))
                .setNegativeButton("Cancel",null).create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window!=null){
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.925),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }


    //checks if the team Id is real and adds the user to the pendingMembers of said team
    private void sendJoinRequest(String teamId) {
        if (teamId != null && !teamId.isBlank()) {
            db.collection("Teams").document(teamId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(this, "Team not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Team team = doc.toObject(Team.class);
                        if (team.isMember(username)) {
                            Toast.makeText(this, "You are already a member of this team", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // add to pending members list
                        db.collection("Teams").document(teamId)
                                .update("pendingMembers", FieldValue.arrayUnion(username))
                                .addOnSuccessListener(docRef ->
                                        Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Log.w("JOIN_REQUEST", "failed to send request", e));
                    })
                    .addOnFailureListener(e -> Log.w("JOIN_REQUEST", "failed to find team", e));
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
