package com.example.campusconnect;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class show_std extends AppCompatActivity {

    TextView std_list;
    EditText search_std;
    Button search;

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
        setContentView(R.layout.activity_show_std);
        LoadingDialog loadingDialog = new LoadingDialog(this);

        std_list = findViewById(R.id.std_list);
        search_std = findViewById(R.id.search_std);  // Assuming search_std is the ID of EditText for search
        search = findViewById(R.id.search);  // Assuming search is the ID of the search button

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, List<Map<String, String>>> classWiseStudents = new HashMap<>(); // To store students grouped by class
        Map<String, Integer> classStudentCount = new HashMap<>(); // To store student count for each class

        // Fetch all students initially
        fetchStudents(db, classWiseStudents, classStudentCount, "");

        // Set up search button click listener
        search.setOnClickListener(v -> {
            loadingDialog.show();
            String searchTerm = search_std.getText().toString().trim().toLowerCase();
            if (!searchTerm.isEmpty()) {
                loadingDialog.dismiss();
                // Fetch students filtered by the search term (name)
                fetchStudents(db, classWiseStudents, classStudentCount, searchTerm);
            } else {
                loadingDialog.dismiss();
                // If search term is empty, show all students
                fetchStudents(db, classWiseStudents, classStudentCount, "");
            }
        });
    }

    private void fetchStudents(FirebaseFirestore db, Map<String, List<Map<String, String>>> classWiseStudents,
                               Map<String, Integer> classStudentCount, String searchTerm) {
        // Clear previous data
        classWiseStudents.clear();
        classStudentCount.clear();

        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder formattedData = new StringBuilder();
                        int studentCount = 0; // Counter for the total number of students

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String studentClass = document.getString("class");
                            String mobileNumber = document.getString("mobile_no");

                            // Handle the stdid field safely
                            Object stdidObject = document.get("stdid"); // Use get() for safe handling of different types
                            String studentId = (stdidObject != null) ? stdidObject.toString() : "N/A"; // Convert to string if not null

                            // Ensure no null values for display
                            if (name == null) name = "N/A";
                            if (email == null) email = "N/A";
                            if (studentClass == null) studentClass = "N/A";
                            if (mobileNumber == null) mobileNumber = "Not Available";

                            // If search term is provided, filter by student name
                            if (searchTerm.isEmpty() || name.toLowerCase().contains(searchTerm)) {
                                studentCount++; // Increment the counter for each student

                                // Add student to the correct class list
                                if (studentClass != null) {
                                    if (!classWiseStudents.containsKey(studentClass)) {
                                        classWiseStudents.put(studentClass, new ArrayList<>());
                                        classStudentCount.put(studentClass, 0); // Initialize student count for the class
                                    }
                                    Map<String, String> studentData = new HashMap<>();
                                    studentData.put("name", name);
                                    studentData.put("email", email);
                                    studentData.put("stdid", studentId);
                                    studentData.put("class", studentClass);
                                    studentData.put("mobile", mobileNumber);

                                    classWiseStudents.get(studentClass).add(studentData);
                                    classStudentCount.put(studentClass, classStudentCount.get(studentClass) + 1); // Increment the student count
                                }
                            }
                        }

                        // Check if any student data was retrieved
                        if (studentCount > 0) {
                            Toast.makeText(getApplicationContext(), "Students retrieved successfully!", Toast.LENGTH_SHORT).show();

                            // Format the class-wise student data
                            formattedData.append("Total Students: ").append(studentCount).append("\n\n");
                            for (Map.Entry<String, List<Map<String, String>>> entry : classWiseStudents.entrySet()) {
                                formattedData.append("Class: ").append(entry.getKey())
                                        .append(" (").append(classStudentCount.get(entry.getKey())).append(" students)").append("\n");

                                for (Map<String, String> student : entry.getValue()) {
                                    formattedData.append("Name: ").append(student.get("name")).append("\n")
                                            .append("Email: ").append(student.get("email")).append("\n")
                                            .append("Student ID: ").append(student.get("stdid")).append("\n")
                                            .append("Mobile: ").append(student.get("mobile")).append("\n")
                                            .append("----------------------------\n");
                                }

                                formattedData.append("\n"); // Add spacing between different classes
                            }

                            // Update UI on the main thread
                            runOnUiThread(() -> std_list.setText(formattedData.toString()));

                        } else {
                            Toast.makeText(getApplicationContext(), "No students found.", Toast.LENGTH_SHORT).show();
                            runOnUiThread(() -> std_list.setText("No students available."));
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to fetch students", Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> std_list.setText("Error fetching students."));
                    }
                });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
