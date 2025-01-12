package com.example.campusconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.campusconnect.R; // Replace "com.example.campusconnect" with your app's package name
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class student_dashboard extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    Button mark_attendence, Getatten;
    ImageView student_logout_btn;
    TextView show_std_name, textview5;
    ListView attendanceListView;
    ArrayAdapter<String> attendanceAdapter;
    List<String> attendanceList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private Handler locationHandler;
    private Runnable locationRunnable;
    private static final long LOCATION_CHECK_INTERVAL = 10000; // 10 seconds
    private static final double TARGET_LATITUDE = 21.6888286; // Replace with your target latitude
    private static final double TARGET_LONGITUDE = 69.7143509; // Replace with your target longitude
    private static final float LOCATION_RANGE = 20.0f; // Range in meters
    private static final String CHANNEL_ID = "location_notification_channel";

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
        setContentView(R.layout.activity_student_dashboard);
        AutoMarkAbsence autoMarkAbsence = new AutoMarkAbsence();
        autoMarkAbsence.fetchAndMarkAbsence();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions
        requestLocationPermission();

        // Initialize Handler
        locationHandler = new Handler(Looper.getMainLooper());
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                checkLocationSettings();
                locationHandler.postDelayed(this, LOCATION_CHECK_INTERVAL);
            }
        };
        locationHandler.post(locationRunnable);
        LoadingDialog loadingDialog = new LoadingDialog(this);

        // Initialize ListView and Adapter
        attendanceListView = findViewById(R.id.showatten);
        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(attendanceAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.shome);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();  // Get the ID of the selected menu item

                if (itemId == R.id.shome) {
                    Intent intent = new Intent(getApplicationContext(), student_dashboard.class);
                    startActivity(intent);
                    finish();
                    return true;

                } else if (itemId == R.id.sprofile) {
                    Intent intent = new Intent(getApplicationContext(), student_profile.class);
                    startActivity(intent);
                    finish();
                    return true;

                } else {
                    return false; // No valid menu item selected
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        student_logout_btn = findViewById(R.id.student_logout_btn);
        student_logout_btn.setOnClickListener(v -> {
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

        LocalDate today = LocalDate.now();
        String todayDate = today.toString();

        // Initialize Firebase Realtime Database reference
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();


        mark_attendence = findViewById(R.id.mark_attendence);
        mark_attendence.setEnabled(false);
        mark_attendence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Ensure user and todayDate are properly initialized
                if (user == null || todayDate == null) {
                    Toast.makeText(student_dashboard.this, "User or date is not initialized", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the email of the currently authenticated user
                String userEmail = user.getEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    Toast.makeText(student_dashboard.this, "User email is not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Show the dialog
                loadingDialog.show();

                // Fetch student details (name, studentId, and class_name) from Firestore using email as the document ID
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("users").document(userEmail).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Retrieve name, studentId, and class_name from Firestore
                            String name = document.getString("name");
                            Long studentIdLong = document.getLong("stdid");
                            String studentId = (studentIdLong != null) ? String.valueOf(studentIdLong) : null;
                            String class_name = document.getString("class");

                            if (name == null || studentId == null || class_name == null) {
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                Toast.makeText(student_dashboard.this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Create attendance data
                            Map<String, Object> attendanceData = new HashMap<>();
                            attendanceData.put("name", name); // Add name to the attendance data
                            attendanceData.put("email", userEmail); // Add student email
                            attendanceData.put("status", "present");

                            // Save attendance under "Attendance/todayDate/class_name/studentId" in Realtime Database
                            database.child("Attendance")
                                    .child(todayDate)      // Parent node for the date
                                    .child(class_name)     // Child node for the class name
                                    .child(studentId)      // Child node for the student ID
                                    .setValue(attendanceData) // Set the data
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "Attendance marked successfully");
                                        // Perform your login task here, and dismiss the dialog when done
                                        loadingDialog.dismiss();
                                        Toast.makeText(student_dashboard.this, "Attendance Marked!", Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firebase", "Error marking attendance", e);
                                        Toast.makeText(student_dashboard.this, "Failed to mark attendance", Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(student_dashboard.this, "User details not found in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error fetching user details", task.getException());
                        Toast.makeText(student_dashboard.this, "Failed to retrieve user details from Firestore", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        Getatten = findViewById(R.id.Getatten);
        Getatten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendanceList.clear(); // Clear the list before adding new data
                // Get the current logged-in user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(student_dashboard.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Perform your login task here, and dismiss the dialog when done
                loadingDialog.show();

                String userEmail = user.getEmail(); // Get the user's email

                // Access Firebase Realtime Database
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                // Retrieve all attendance records
                database.child("Attendance").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            List<String> dates = new ArrayList<>();

                            // Collect all dates
                            for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                                dates.add(dateSnapshot.getKey()); // Add each date to the list
                            }

                            // Sort the dates in descending order
                            Collections.sort(dates, Collections.reverseOrder());

                            // Iterate over the sorted dates
                            for (String date : dates) {
                                DataSnapshot dateSnapshot = snapshot.child(date);

                                // Iterate over all classes under the date
                                for (DataSnapshot classSnapshot : dateSnapshot.getChildren()) {
                                    // Iterate over all student records under the class
                                    for (DataSnapshot studentSnapshot : classSnapshot.getChildren()) {
                                        String email = studentSnapshot.child("email").getValue(String.class);

                                        // Match the logged-in user's email
                                        if (email != null && email.equals(userEmail)) {
                                            String name = studentSnapshot.child("name").getValue(String.class);
                                            String status = studentSnapshot.child("status").getValue(String.class);

                                            // Add attendance details to the list
                                            String attendanceRecord = "Date: " + date + "\n"
                                                    + "Name: " + name + "\n"
                                                    + "Status: " + status;
                                            // Perform your login task here, and dismiss the dialog when done
                                            loadingDialog.dismiss();
                                            attendanceList.add(attendanceRecord);
                                        }
                                    }
                                }
                            }

                            // Notify the adapter about the data change
                            attendanceAdapter.notifyDataSetChanged();

                            if (attendanceList.isEmpty()) {
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                Toast.makeText(student_dashboard.this, "No attendance records found for this user.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Perform your login task here, and dismiss the dialog when done
                            loadingDialog.dismiss();
                            Toast.makeText(student_dashboard.this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error retrieving data", error.toException());
                        Toast.makeText(student_dashboard.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(user.getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve name, studentId, and class_name from Firestore
                    String name1 = document.getString("name");
                    show_std_name = findViewById(R.id.show);
                    show_std_name.setText("welcome, " + name1 + "\n" + user.getEmail());
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission already granted
            checkLocationSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                checkLocationSettings();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to mark attendance.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_CHECK_INTERVAL)
                .setFastestInterval(LOCATION_CHECK_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. Initialize location requests here.
                    getLocation();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(student_dashboard.this, REQUEST_LOCATION_PERMISSION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Toast.makeText(student_dashboard.this, "Location settings are inadequate, and cannot be fixed here. Please enable location services manually.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHandler != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_CHECK_INTERVAL)
                    .setFastestInterval(LOCATION_CHECK_INTERVAL);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    currentLocation = location;
                    checkLocationInRange();
                }
            }
        }
    };
    int v=0;
    private void checkLocationInRange() {
        if (currentLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    TARGET_LATITUDE, TARGET_LONGITUDE, results);
            float distanceInMeters = results[0];
            String d = String.valueOf(distanceInMeters);


            if (distanceInMeters <= LOCATION_RANGE) {
                v=v+1;
                mark_attendence.setEnabled(true);
                textview5 = findViewById(R.id.textView5);
                //textview5.setText("latitude:-" + currentLocation.getLatitude() + ",longi:-" + currentLocation.getLongitude() + "distance:" + d);
                textview5.setText("🟢😃You are Within Range");
                textview5.setTextColor(Color.parseColor("#28A745"));

                if(v==1)
                {
                    sendNotification("You are within range. You can mark attendance.");
                    Toast.makeText(this, "You are within range. You can mark attendance.", Toast.LENGTH_SHORT).show();
                    v=2;

                }
            }
            else
            {
                v=0;
                textview5 = findViewById(R.id.textView5);
                textview5.setText("🔴☹️You are out-of Range");
                textview5.setTextColor(Color.parseColor("#9B111E"));
                mark_attendence.setEnabled(false);
                //textview5.setText("latitude:-" + currentLocation.getLatitude() + ",longi:-" + currentLocation.getLongitude() + "distance:" + d);
                Toast.makeText(this, "You are out of range. Attendance marking is disabled.", Toast.LENGTH_SHORT).show();
                sendNotification("You are out of range. Attendance marking is disabled.");
            }
        }
    }

    private void sendNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Location Notification";
            String description = "Notification channel for location updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.campusconnecct) // Replace with your app's icon
                .setContentTitle("Location Update")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Notify
        notificationManager.notify(1, builder.build());
    }
    // Other existing methods...
}
