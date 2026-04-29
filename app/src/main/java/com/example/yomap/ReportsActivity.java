package com.example.yomap;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Dialog reportD, reportVD;
    TextView severityDisplay, viewSender, viewGroup ,viewIssue, viewDate, viewSeverity;
    EditText editIssue, editGroup;
    SeekBar severityBar;
    int severityVal;
    RecyclerView recyclerViewReports;
    Button back, newreport, sendReport;
    Spinner sortby, status;
    ArrayList<Report> reports = new ArrayList<>();
    myAdapter<Report> adapter;
    String username, id;
    Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initializing components
        back = findViewById(R.id.backToTeam);
        newreport = findViewById(R.id.buttonNewReport);
        recyclerViewReports = findViewById(R.id.reportList);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        sortby = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortby.setAdapter(sortAdapter);
        username = UserSession.getUsername();
        id = getIntent().getStringExtra("teamId");
        CollectionReference colRef = db.collection("Teams").document(id).collection("Reports");
        db.collection("Teams").document(id).get()
                .addOnSuccessListener(docReff -> {
                    team = docReff.toObject(Team.class);
                    colRef.get()
                            .addOnSuccessListener(docRef -> {

                                reports.clear();

                                for (DocumentSnapshot doc : docRef.getDocuments()) {
                                    Report report = doc.toObject(Report.class);

                                    if (report != null) {
                                        report.setId(doc.getId());

                                        if (team.isManager(username) || report.getSender().equals(username)) {
                                            reports.add(report);
                                        }
                                    }
                                }

                                adapter = new myAdapter(
                                        reports,
                                        position -> reportDialogView(position),
                                        (view, position) -> {
                                        }
                                );

                                recyclerViewReports.setAdapter(adapter);
                                sortReports(0); //starting up, the reports are sorted from newest to oldest
                            })
                            .addOnFailureListener(e ->
                                    Log.w("fail_load_reports", "fail loading reports", e));
                });
        sortby.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null) {
                    sortReports(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        back.setOnClickListener(v -> finish());
        newreport.setOnClickListener(v -> reportDialogNew());
     }

    private void reportDialogView(int pos) {
        reportVD = new Dialog(this);
        reportVD.setContentView(R.layout.report_display_dialog);
        reportVD.setTitle("Report");
        reportVD.setCancelable(true);

        Report thisreport = reports.get(pos);
        viewSender = reportVD.findViewById(R.id.viewSender);
        viewGroup = reportVD.findViewById(R.id.viewGroup);
        viewDate = reportVD.findViewById(R.id.viewDate);
        viewIssue = reportVD.findViewById(R.id.viewIssue);
        viewSeverity = reportVD.findViewById(R.id.viewSeverity);
        status = reportVD.findViewById(R.id.spinnerStatus);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.status,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        String severitys = "Severity: " +thisreport.getSeverity();
        viewSender.setText(thisreport.getSender());
        viewGroup.setText((thisreport.getGroup()));
        viewDate.setText(UserSession.timeToString(thisreport.getDate()));
        viewIssue.setText(thisreport.getIssue());
        viewSeverity.setText(severitys);

        status.setAdapter(adapter);
        status.setSelection(thisreport.getStatus());
        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long ids) {
                String selected = parent.getItemAtPosition(position).toString();
                Log.d("SPINNER", "Selected: " + selected);
                db.collection("Teams").document(id).collection("Reports")
                        .document(thisreport.getId()).update("status", position)
                        .addOnSuccessListener(docRef -> {
                            thisreport.setStatus(position);
                            adapter.notifyDataSetChanged();
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        reportVD.show();
        Window window = reportVD.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.925),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }
    private void reportDialogNew() {
        if (reports != null) {
            reportD = new Dialog(this);
            reportD.setContentView(R.layout.report_dialog);
            reportD.setTitle("New Report");
            reportD.setCancelable(true);

            editIssue = reportD.findViewById(R.id.editIssue);
            editGroup = reportD.findViewById(R.id.editGroup);
            severityBar = reportD.findViewById(R.id.severityBar);
            sendReport = reportD.findViewById(R.id.sendReport);
            severityDisplay = reportD.findViewById(R.id.severityDisplay);

            severityVal = 1;
            severityBar.setProgress(4); //start on the middle
            severityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    severityVal = progress + 1; // convert to 1–10
                    String severityString = "Severity: " + severityVal;
                    severityDisplay.setText(severityString);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            sendReport.setOnClickListener(v -> sendReport());

            reportD.show();
            Window window = reportD.getWindow();
            if (window != null) {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.925),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }

        }
    }


    private void sendReport() {
        String issue = editIssue.getText().toString();
        String group = editGroup.getText().toString();
        Report newreport = new Report(username, issue, group, severityVal, 0);
        db.collection("Teams").document(id).collection("Reports")
                .add(newreport).addOnSuccessListener(docRef -> {
                    newreport.setId(docRef.getId());
                    reports.add(newreport);
                    reportD.dismiss();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e-> Log.w("SENDING", "fail sending report",e));
    }

    private void sortReports(int sortOption) {
        switch (sortOption) {
            case 0: // Newest First
                Collections.sort(reports, (a, b) ->
                        b.getDate().compareTo(a.getDate()));
                break;
            case 1: // Oldest First
                Collections.sort(reports, (a, b) ->
                        a.getDate().compareTo(b.getDate()));
                break;
            case 2: // Severity High to Low
                Collections.sort(reports, (a, b) ->
                        Integer.compare(b.getSeverity(), a.getSeverity()));
                break;
            case 3: // Severity Low to High
                Collections.sort(reports, (a, b) ->
                        Integer.compare(a.getSeverity(), b.getSeverity()));
                break;
            case 4: // Status Unresolved to Resolved
                Collections.sort(reports, (a, b) ->
                        Integer.compare(a.getStatus(), b.getStatus()));
                break;
            case 5: // Status Resolved to Unresolved
                Collections.sort(reports, (a, b) ->
                        Integer.compare(b.getStatus(), a.getStatus()));
                break;
        }
        adapter.notifyDataSetChanged();
    }

}

