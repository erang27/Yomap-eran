package com.example.yomap;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    Dialog memberD, memberPD;
    RecyclerView membersView, pendingMembersView;
    myAdapter<String> adapterMember, adapterPending;
    ArrayList<String> members, pendingMembers;
    TextView teamView;
    EditText editAddUserToTeam;
    View spaceAboveMembers;
    Button goHome, showMembers, moveToReport,addUserToTeam, buttonLeave, buttonRemove, buttonPend, buttonId;
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
        buttonPend = findViewById(R.id.pendingMembers);
        buttonId = findViewById(R.id.buttonId);
        spaceAboveMembers = findViewById(R.id.spaceAboveMembers); //in case the user isnt a manager, makes up for the pending button dissappearing
        id = getIntent().getStringExtra("teamId");
        username = UserSession.getUsername();

        //button functionality
        goHome.setOnClickListener(v -> finish());
        showMembers.setOnClickListener(v -> memberDialog());
        moveToReport.setOnClickListener(v -> {
            Intent intent = new Intent(TeamActivity.this, ReportsActivity.class);
            intent.putExtra("teamId", id);
            activityResultLauncher.launch(intent);
        });
        buttonId.setOnClickListener(v -> showId());
        buttonPend.setOnClickListener(v -> pendingMembersDialog());
        buttonLeave.setOnClickListener(v -> leaveTeam());
        buttonRemove.setOnClickListener(v-> deleteTeam());
    }

    protected void onResume() {
        super.onResume();
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
                    if (isManager) {
                        buttonPend.setVisibility(View.VISIBLE);
                        spaceAboveMembers.setVisibility(View.VISIBLE);
                    }
                    else {
                        buttonPend.setVisibility(View.GONE);
                        spaceAboveMembers.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.w("failgettingteam", "fail getting team", e));

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
            adapterMember = new myAdapter<String>(members, position -> {
            }, (view,position) -> popupMembers(view, position), user-> team.isManager(user));
            membersView.setAdapter(adapterMember);
            adapterMember.notifyDataSetChanged();
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
                adapterMember.notifyDataSetChanged();
                editAddUserToTeam.setText("");
            });
            memberD.show();
            Window window = memberD.getWindow();
            if (window!=null){
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.925),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }
            membersView.getLayoutParams().height = (int)(getResources().getDisplayMetrics().heightPixels * 0.5);;
            membersView.requestLayout(); //updates the list's height
        }
    }

    private void pendingMembersDialog() {
        if (team != null) {
            memberPD = new Dialog(this);
            memberPD.setContentView(R.layout.pending_member_dialog);
            memberPD.setTitle("Pending Members");
            memberPD.setCancelable(true);
            pendingMembersView = memberPD.findViewById(R.id.pendingMembersList);
            pendingMembersView.setLayoutManager(new LinearLayoutManager(this));
            pendingMembers = new ArrayList<>(team.getPendingUsers());
            adapterPending = new myAdapter(pendingMembers, position -> {
            }, (view,position) -> popupPending(view, position));
            pendingMembersView.setAdapter(adapterPending);
            adapterPending.notifyDataSetChanged();
            memberPD.show();
            Window window = memberPD.getWindow();
            if (window!=null){
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.925),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }
            pendingMembersView.getLayoutParams().height = (int)(getResources().getDisplayMetrics().heightPixels * 0.5);;
            pendingMembersView.requestLayout(); //updates the list's height
        }
    }

    //handle click on pendingMember
    private void popupPending(View view, int position) {
        String selectedUser = pendingMembers.get(position);
        if (isManager && !selectedUser.equals(username) && !team.isFounder(selectedUser))
        {
            PopupMenu popup = new PopupMenu(this, view);
            popup.inflate(R.menu.list_item_menu_pending);
            Menu menu = popup.getMenu();
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_accept) {
                    unpendUser(selectedUser, true);
                    return true;
                }
                else if (item.getItemId() == R.id.action_reject) {
                    unpendUser(selectedUser, false);
                    return true; }
                return false;
            });
            popup.show();
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

    //removing the user from the request list, either adds to team or dismisses
    private void unpendUser(String user, boolean gotin) {
        if (!gotin) {
            db.collection("Teams").document(id).update("pendingMembers", FieldValue.arrayRemove(user))
                    .addOnSuccessListener(docRef -> {
                        team.unpendMember(user, false);
                        pendingMembers.remove(user);
                        adapterPending.notifyDataSetChanged();
                        Toast.makeText(this, "User rejected successfully", Toast.LENGTH_SHORT).show();
                    });
            return;
        }
        // check if addition is possible before removing from pending
        db.collection("Users").document(user).get()
                .addOnSuccessListener(docReff -> {
                    if (!docReff.exists()) {
                        Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    User u = docReff.toObject(User.class);
                    if (u != null && u.getTeamIds() != null && u.getTeamIds().size() > 9) {
                        Toast.makeText(this, "User has too many teams", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.collection("Teams").document(id).update("pendingMembers", FieldValue.arrayRemove(user))
                            .addOnSuccessListener(docRef -> {
                                team.unpendMember(user, true);
                                pendingMembers.remove(user);
                                adapterPending.notifyDataSetChanged();
                                addUserToTeam(user);
                            });
                });
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
                                adapterMember.notifyDataSetChanged();
                                Toast.makeText(this,"user removed successfully", Toast.LENGTH_SHORT).show();
                                if (team.isManager(exuser)) {demoteManager(exuser); } //if a founder removes a manager
                            });
                });
    }

    //leaving the team requires a handling for a case of the founder leaving
    private void leaveTeam() {
        if (isFounder) {
            if (team.getManagers().size()>=2) {
                String newFounder = team.getManagers().get(1);
                db.collection("Teams").document(id).update("founder", newFounder)
                        .addOnSuccessListener(docRef-> team.setFounder(newFounder));
            }
            else if (team.getMembers().size()>=2) {
                makeManager(team.getMembers().get(1));
            }
            else {
                deleteTeam(); //if there are no other members, the team is removed when the founder leaves
                return;
            }
        }
        removeUserFromTeam(username);
        finish();
    }

    //allows the user to copy to the id to clipboard
    private void showId() {
        TextView textId = new TextView(this);
        textId.setText(id);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Team ID").setView(textId)
                .setPositiveButton("Copy ID", (d,ok)-> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("teamId", id);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "ID copied!", Toast.LENGTH_SHORT).show();
                })
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

    //erases the team by kicking all of the users out
    private void deleteTeam() {
        List<String> membersCopy = new ArrayList<>(team.getMembers());
        for (int i = 0; i<membersCopy.size(); i++) {
            removeUserFromTeam(membersCopy.get(i));
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

    
    //gets a string, if it's linked to an actual username, the user is added to the team. if the addition is successfull, returns true.
    private void addUserToTeam(String newuser) {
        if (newuser == null || newuser.isBlank()) {
            Toast.makeText(TeamActivity.this, "unable to add user to the team", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (team.isMember(newuser)) {
            Toast.makeText(this, "the user is already in the team", Toast.LENGTH_SHORT).show();
            return ;
        }

        db.collection("Users").document(newuser).get()
                .addOnSuccessListener(docReff -> {
                    if (!docReff.exists()) {
                        Toast.makeText(this, "user doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        User user = docReff.toObject(User.class);
                        if (user != null && user.getTeamIds() != null && user.getTeamIds().size() > 9) {
                            Toast.makeText(this, "user has too many teams", Toast.LENGTH_SHORT).show();
                        } else {
                            db.collection("Teams").
                                    document(id).update("users", FieldValue.arrayUnion(newuser))
                                    .addOnSuccessListener(docRef -> {
                                        db.collection("Users").document(newuser)
                                                .update("teamIds", FieldValue.arrayUnion(id))
                                                .addOnSuccessListener(userRef -> {
                                                    team.addUsers(newuser);
                                                    if (members != null) members.add(newuser);
                                                    if (adapterMember != null) adapterMember.notifyDataSetChanged();
                                                    Toast.makeText(this,"user added successfully", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("fail adding user to team", "fail adding user to team", e);
                                    });
                        }
                    }
                });


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
