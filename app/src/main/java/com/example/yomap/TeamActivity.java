package com.example.yomap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Dialog memberD;
    ListView membersView;
    ArrayAdapter<String> adapter;
    ArrayList<String> members;
    TextView teamView;
    EditText editAddUserToTeam;
    Button goHome, showMembers, moveToReport,addUserToTeam;
    String id, username;
    Team team;
    boolean isManager, isFounder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_team);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //initializing components
        teamView = findViewById(R.id.teamView);
        showMembers = findViewById(R.id.showMembers);
        goHome = findViewById(R.id.buttonHome);
        moveToReport = findViewById(R.id.buttonReports);
        id = getIntent().getStringExtra("teamId");
        username = UserSession.getUsername();
        //registerForContextMenu(membersView);
        db.collection("Teams").document(id).get()
                .addOnSuccessListener(docRef -> {
                    team = docRef.toObject(Team.class);
                    teamView.setText(team.getTitle());
                    isManager = team.isManager(username);
                    isFounder = team.isFounder(username);
                })
                .addOnFailureListener(e -> Log.w("failgettingteam", "fail getting team", e));

        //button functionality
        goHome.setOnClickListener(v -> finish());
        showMembers.setOnClickListener(v -> memberDialog());
        moveToReport.setOnClickListener(v -> {
            Intent intent = new Intent(TeamActivity.this, ReportsActivity.class);
            intent.putExtra("teamId", id);
            activityResultLauncher.launch(intent);
        });
    }

    private void memberDialog() {
        if (team!=null) {
            memberD = new Dialog(this);
            memberD.setContentView(R.layout.member_dialog);
            memberD.setTitle("Members");
            memberD.setCancelable(true);
            membersView = memberD.findViewById(R.id.membersList);
            members = new ArrayList<>(team.getMembers());
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, members);
            membersView.setAdapter(adapter);
            addUserToTeam = memberD.findViewById(R.id.buttonAddToTeam);
            editAddUserToTeam = memberD.findViewById(R.id.addUser);

            //if not manager, user-adding UI is gone
            if (isManager) {
                addUserToTeam.setVisibility(View.VISIBLE);
                editAddUserToTeam.setVisibility(View.VISIBLE);
            }
            else {
                addUserToTeam.setVisibility(View.GONE);
                editAddUserToTeam.setVisibility(View.GONE);
            }
            addUserToTeam.setOnClickListener(v -> {
                addUserToTeam(editAddUserToTeam.getText().toString());
                adapter.notifyDataSetChanged();
                editAddUserToTeam.setText("");
            });

            //popup menu when pressing a listview item
            membersView.setOnItemLongClickListener((parent, view, position, id) -> {
                        if (isManager) {
                            PopupMenu popup = new PopupMenu(this, view);
                            popup.inflate(R.menu.list_item_menu_members);
                            Menu menu = popup.getMenu();
                            //todo: make the popup specific to scenerios of manager and founder, also to self
                            if (!isManager) {

                            }
                            if (team.isManager(members.get(position))) {
                                menu.findItem(R.id.action_promote).setVisible(false);
                                menu.findItem(R.id.action_demote).setVisible(isFounder);
                                menu.findItem(R.id.action_delete).setVisible(isFounder);
                            }


                            popup.setOnMenuItemClickListener(item -> {
                                if (item.getItemId() == R.id.action_promote) {
                                    makeManager(members.get(position));
                                    return true;
                                } else if (item.getItemId() == R.id.action_delete) {
                                    removeUserFromTeam(members.get(position));
                                    return true;
                                } else if (item.getItemId() == R.id.action_demote) {
                                    demoteManager(members.get(position));
                                    return true;
                                }
                                return false;
                            });

                            popup.show();
                            return true; // consume long press
                        }
                        return false;
            });



            memberD.show(); }
    }

    //if the selected user isnt already a manager, they becomes one.
    private void makeManager(String newmanager) {
        if (team.isManager(newmanager)) {
            Toast.makeText(this, "user is already a manager", Toast.LENGTH_SHORT).show();
        }
        else {
            db.collection("Teams").document(id).update("managers", FieldValue.arrayUnion(newmanager))
                            .addOnSuccessListener(docRef -> {
                                team.addManagers(newmanager);
                            })
                            .addOnFailureListener(e-> {
                                Log.w("MAKE_MANAGER", "makeManager failed", e);
                                    });
            team.addManagers(newmanager);
        }
    }

    //takes away the manager role from a user
    private void demoteManager(String exmanager) {
        db.collection("Teams").document(id).update("managers", FieldValue.arrayRemove(exmanager))
                .addOnSuccessListener(docRef-> {
                    team.demoteManager(exmanager);
                    //update the listview color
                });

    }

    //removes a user from the team //TODO: handle removing managers as the founder
    private void removeUserFromTeam(String exuser) {

        db.collection("Users").document(exuser).update("teamIds", FieldValue.arrayRemove(id))
                .addOnSuccessListener(docRef -> {
                    db.collection("Teams").document(id).update("users", FieldValue.arrayRemove(exuser))
                            .addOnSuccessListener(docRef1 -> {
                                team.removeMember(exuser);
                                members.remove(exuser);
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    
    //gets a string, if it's linked to an actual username, the user is added to the team
    private void addUserToTeam(String newuser) {
        if (newuser != null && !newuser.isBlank() && !team.isMember(newuser)) {

            db.collection("Users").document(newuser).get()
                            .addOnSuccessListener(docReff -> {
                                if (docReff.exists()) {
                                    db.collection("Teams").
                                            document(id).update("users", FieldValue.arrayUnion(newuser))
                                            .addOnSuccessListener(docRef -> {
                                                db.collection("Users").document(newuser)
                                                        .update("teamIds", FieldValue.arrayUnion(id))
                                                        .addOnSuccessListener(userRef -> {
                                                            team.addUsers(newuser);
                                                            members.add(newuser);
                                                            adapter.notifyDataSetChanged();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w("fail adding user to team", "fail adding user to team",e);
                                            });
                                }
                                else {
                                    Toast.makeText(TeamActivity.this, "User doesnt exist", Toast.LENGTH_SHORT).show();
                                }
                            });

        }
        else {
            Toast.makeText(TeamActivity.this, "unable to add user to the team", Toast.LENGTH_SHORT).show();
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
