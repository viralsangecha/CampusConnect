package com.example.campusconnect;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class teacher_dashboard extends AppCompatActivity {

    ImageView logot_btn;
    TextView show_tea_name,record_cat;
    ListView today_list;
    EditText search_input;
    Button search_button;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    ArrayAdapter<String> adapter;
    ArrayList<String> attendanceData = new ArrayList<>();
    String todaydate;

    @Override
    protected void onStart() {
        super.onStart();
        if (!isNetworkAvailable()) {
            // Redirect to No Internet Activity
            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);
        record_cat = findViewById(R.id.record_category); // Correctly initialize record_cat

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must log in to access this feature.", Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.thome);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.thome) {
                    startActivity(new Intent(getApplicationContext(), teacher_dashboard.class));
                    finish();
                    return true;
                } else if (itemId == R.id.addstd) {
                    startActivity(new Intent(getApplicationContext(), add_student.class));
                    finish();
                    return true;
                } else if (itemId == R.id.tprofile) {
                    startActivity(new Intent(getApplicationContext(), teacher_profile.class));
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        });


        logot_btn = findViewById(R.id.logout_btn);
        logot_btn.setOnClickListener(v -> {
            // Create an AlertDialog to confirm logout
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Perform logout
                        auth.signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });


        show_tea_name = findViewById(R.id.show_tea_name);
        show_tea_name.setText("Welcome, " + user.getEmail());
        today_list = findViewById(R.id.today_list);
        search_input = findViewById(R.id.search_input);
        search_button = findViewById(R.id.search_button);


        search_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePickerDialog();
            }
        });
        search_input.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
          @Override
          public void onFocusChange(View v, boolean hasFocus)
          {
              openDatePickerDialog();
          }
        });


        todaydate=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceData);
        today_list.setAdapter(adapter);
        loadAttendanceByDate(todaydate);

        search_button.setOnClickListener(v -> {
            String query = search_input.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter a name or date to search.", Toast.LENGTH_SHORT).show();
                // Load today's attendance by default
                loadAttendanceByDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                record_cat.setText("-:Today Present Students:-");
                return;
            }

            if (query.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // If query matches date format YYYY-MM-DD
                record_cat.setText("Records of " + query);
                loadAttendanceByDate(query);
            } else {
                // Otherwise, search by student name
                record_cat.setText("Records of " + query);
                searchAttendanceByName(query);
            }
        });
    }

    public void loadAttendanceByDate(String date) {

        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Attendance").child(date);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceData.clear();
                HashMap<String, Integer> presentCountMap = new HashMap<>(); // Class-wise present count
                HashMap<String, Integer> absentCountMap = new HashMap<>();  // Class-wise absent count

                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String className = classSnapshot.getKey();
                    int classPresent = 0;
                    int classAbsent = 0;

                    attendanceData.add("Class: " + className);

                    for (DataSnapshot studentSnapshot : classSnapshot.getChildren()) {
                        String name = studentSnapshot.child("name").getValue(String.class);
                        String email = studentSnapshot.child("email").getValue(String.class);
                        String status = studentSnapshot.child("status").getValue(String.class);

                        if ("Present".equalsIgnoreCase(status)) {
                            classPresent++;
                        } else if ("Absent".equalsIgnoreCase(status)) {
                            classAbsent++;
                        }

                        if (date.matches(todaydate)) {
                            record_cat.setText("-:Today Present Students:-");
                        } else {
                            record_cat.setText("Records of " + date);
                        }

                        attendanceData.add("  Name: " + name + "\n  Email: " + email + "\n  Status: " + status);
                    }

                    // Store class-wise counts
                    presentCountMap.put(className, classPresent);
                    absentCountMap.put(className, classAbsent);

                    attendanceData.add("----------------------------");
                }

                // Add class-wise summary to the attendance data
                attendanceData.add("Class-Wise Summary:");
                for (String className : presentCountMap.keySet()) {
                    attendanceData.add("Class: " + className);
                    attendanceData.add("  Total Present: " + presentCountMap.get(className));
                    attendanceData.add("  Total Absent: " + absentCountMap.get(className));
                    attendanceData.add("----------------------------");
                }

                if (attendanceData.isEmpty()) {
                    record_cat.setText("Records of " + date);
                    attendanceData.add("No attendance data available for " + date + ".");
                }

                loadingDialog.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void searchAttendanceByName(String name) {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Attendance");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceData.clear();
                HashMap<String, Integer> presentCountMap = new HashMap<>(); // Class-wise present count
                HashMap<String, Integer> absentCountMap = new HashMap<>();  // Class-wise absent count

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    for (DataSnapshot classSnapshot : dateSnapshot.getChildren()) {
                        String className = classSnapshot.getKey();

                        for (DataSnapshot studentSnapshot : classSnapshot.getChildren()) {
                            String studentName = studentSnapshot.child("name").getValue(String.class);
                            String email = studentSnapshot.child("email").getValue(String.class);
                            String status = studentSnapshot.child("status").getValue(String.class);

                            if (name.equalsIgnoreCase(studentName)) {
                                attendanceData.add("Date: " + date);
                                attendanceData.add("  Name: " + studentName + "\n  Email: " + email + "\n  Status: " + status);
                                attendanceData.add("----------------------------");

                                // Update class-wise counters
                                if ("Present".equalsIgnoreCase(status)) {
                                    presentCountMap.put(className, presentCountMap.getOrDefault(className, 0) + 1);
                                } else if ("Absent".equalsIgnoreCase(status)) {
                                    absentCountMap.put(className, absentCountMap.getOrDefault(className, 0) + 1);
                                }
                            }
                        }
                    }
                }

                if (attendanceData.isEmpty()) {
                    attendanceData.add("No attendance data found for " + name + ".");
                } else {
                    // Add class-wise summary
                    attendanceData.add("Class-Wise Summary for " + name + ":");
                    for (String className : presentCountMap.keySet()) {
                        attendanceData.add("Class: " + className);
                        attendanceData.add("  Total Present: " + presentCountMap.get(className));
                        attendanceData.add("  Total Absent: " + absentCountMap.getOrDefault(className, 0));
                        attendanceData.add("----------------------------");
                    }
                }

                loadingDialog.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                    loadAttendanceByDate(date); // Your search method
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
