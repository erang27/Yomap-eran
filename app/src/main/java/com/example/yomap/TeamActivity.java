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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Dialog memberD;
    RecyclerView membersView;
    myAdapter<String> adapter;
    ArrayList<String> members;
    TextView teamView;
    EditText editAddUserToTeam;
    Button goHome, showMembers, moveToReport,addUserToTeam, buttonLeave, buttonRemove;
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
        buttonLeave = findViewById(R.id.leaveTeam);
        buttonRemove = findViewById(R.id.removeTeam);
        id = getIntent().getStringExtra("teamId");
        username = UserSession.getUsername();
        //registerForContextMenu(membersView);
        db.collection("Teams").document(id).get()
                .addOnSuccessListener(docRef -> {
                    team = docRef.toObject(Team.class);
                    teamView.setText(team.getTitle());
                    isManager = team.isManager(username);
                    isFounder = team.isFounder(username);
                    if (isFounder) {
                        buttonRemove.setVisibility(View.VISIBLE);
                    }
                    else buttonRemove.setVisibility(View.GONE);
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
        buttonLeave.setOnClickListener(v -> leaveTeam());
        buttonRemove.setOnClickListener(v-> deleteTeam());
    }

    private void memberDialog() {
        if (team != null) {
            memberD = new Dialog(this);
            memberD.setContentView(R.layout.member_dialog);
            memberD.setTitle("Members");
            memberD.setCancelable(true);
            membersView = memberD.findViewById(R.id.membersList);
            membersView.setLayoutManager(new LinearLayoutManager(this));
            members = new ArrayList<>(team.getMembers());
            adapter = new myAdapter(members, position -> {
            }, (view,position) -> popupMembers(view, position));
            membersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            addUserToTeam = memberD.findViewById(R.id.buttonAddToTeam);
            editAddUserToTeam = memberD.findViewById(R.id.addUser);

            //if not manager, user-adding UI is gone
            if (isManager) {
                addUserToTeam.setVisibility(View.VISIBLE);
                editAddUserToTeam.setVisibility(View.VISIBLE);
            } else {
                addUserToTeam.setVisibility(View.GONE);
                editAddUserToTeam.setVisibility(View.GONE);
            }
            addUserToTeam.setOnClickListener(v -> {
                addUserToTeam(editAddUserToTeam.getText().toString());
                adapter.notifyDataSetChanged();
                editAddUserToTeam.setText("");
            });
            memberD.show();
        }
    }
    //handle click on member
    private void popupMembers(View view, int position) {
        String selectedUser = members.get(position);
        if (isManager && !selectedUser.equals(username) && !team.isFounder(selectedUser))
        //only managers have access to the popup, and it is impossible to commit actions on yourself or on the founder
        {
            PopupMenu popup = new PopupMenu(this, view);
            popup.inflate(R.menu.list_item_menu_members);
            Menu menu = popup.getMenu();
            if (team.isManager(selectedUser)) {
                menu.findItem(R.id.action_promote).setVisible(false);
                menu.findItem(R.id.action_demote).setVisible(isFounder);
                menu.findItem(R.id.action_delete).setVisible(isFounder);
            } else {
                menu.findItem(R.id.action_promote).setVisible(isFounder);
                menu.findItem(R.id.action_demote).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(true);
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

        }
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

    //removes a user from the team
    private void removeUserFromTeam(String exuser) {

        db.collection("Users").document(exuser).update("teamIds", FieldValue.arrayRemove(id))
                .addOnSuccessListener(docRef -> {
                    db.collection("Teams").document(id).update("users", FieldValue.arrayRemove(exuser))
                            .addOnSuccessListener(docRef1 -> {
                                team.removeMember(exuser);
                                members.remove(exuser);
                                adapter.notifyDataSetChanged();
                                if (team.isManager(exuser)) {demoteManager(exuser); } //if a founder removes a manager
                            });
                });
    }

    //leaving the team requires a handling for a case of the founder leaving
    private void leaveTeam() {
        if (isFounder) {
            if (team.getManagers().size()>=2) {
                team.setFounder(team.getManagers().get(1));
            }
            else if (team.getMembers().size()>=2) {
                makeManager(team.getMembers().get(1));
            }
            else deleteTeam(); //if there are no other members, the team is removed when the founder leaves
        }
        removeUserFromTeam(username);
        finish();
    }

    //erases the team by kicking all of the users out
    private void deleteTeam() {
        for (int i = 0; i<members.size(); i++) {
            removeUserFromTeam(members.get(i));
        }
        db.collection("Teams").document(id).delete()
                .addOnSuccessListener(docRef -> {
                    Log.w("DELETE_TEAM", "team removed successfully");
                    Toast.makeText(this, "team successfully removed", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("DELETE_TEAM", "team couldnt be removed", e);
                });
        finish();
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
