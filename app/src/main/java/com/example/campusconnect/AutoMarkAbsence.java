package com.example.campusconnect;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AutoMarkAbsence {

    FirebaseDatabase database;
    DatabaseReference attendanceRef;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    String name;
    Long studentIdLong;
    String studentId;
    String class_name;
    LocalDate today = LocalDate.now();
    String todayDate = today.toString();

    public AutoMarkAbsence() {
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
    }

    public void fetchAndMarkAbsence() {
        // Fetch student details from Firestore and mark absence
        fetchStudentDetails(new FetchStudentDetailsCallback() {
            @Override
            public void onFetchComplete() {
                // Once the student details are fetched, initialize the attendance reference
                attendanceRef = database.getReference("Attendance").child(todayDate).child(class_name).child(studentId);
                checkAndMarkAbsence();
            }

            @Override
            public void onFetchFailed(String error) {
                Log.e("AutoMarkAbsence", "Error fetching student details: " + error);
            }
        });
    }

    private void fetchStudentDetails(FetchStudentDetailsCallback callback) {
        // Fetch student details (name, studentId, and class_name) from Firestore using email as the document ID
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(user.getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve name, studentId, and class_name from Firestore
                    name = document.getString("name");
                    studentIdLong = document.getLong("stdid");
                    studentId = (studentIdLong != null) ? String.valueOf(studentIdLong) : null;
                    class_name = document.getString("class");

                    // Call the callback once data is fetched
                    callback.onFetchComplete();
                } else {
                    callback.onFetchFailed("Document does not exist");
                }
            } else {
                callback.onFetchFailed(task.getException() != null ? task.getException().getMessage() : "Unknown error");
            }
        });
    }

    // Check if the student has marked their attendance for the current date
    private void checkAndMarkAbsence() {
        if (attendanceRef == null || studentId == null) {
            Log.e("AutoMarkAbsence", "Invalid attendance reference or student ID");
            return;
        }

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // If attendance is not marked, mark the student as absent
                    markAbsent();
                } else {
                    Log.d("AutoMarkAbsence", "Attendance already marked for today.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Log.e("AutoMarkAbsence", "Error checking attendance: " + databaseError.getMessage());
            }
        });
    }

    // Method to mark the student as absent
    private void markAbsent() {
        if (name == null || studentId == null || class_name == null) {
            Log.e("AutoMarkAbsence", "Missing student details");
            return;
        }

        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("email",user.getEmail() );
        attendanceData.put("name", name);
        attendanceData.put("status", "absent");

        // Update the Firebase Realtime Database to mark the student as absent
        attendanceRef.updateChildren(attendanceData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AutoMarkAbsence", "Student marked as absent.");
                })
                .addOnFailureListener(e -> {
                    Log.e("AutoMarkAbsence", "Failed to mark student as absent: " + e.getMessage());
                });
    }

    // Callback interface for fetching student details
    interface FetchStudentDetailsCallback {
        void onFetchComplete();
        void onFetchFailed(String error);
    }
}
