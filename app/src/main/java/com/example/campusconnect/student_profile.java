package com.example.campusconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class student_profile extends AppCompatActivity {

    TextView name,email;
    EditText password;
    Button update_password;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_profile);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.sprofile);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();  // Get the ID of the selected menu item

                if (itemId == R.id.shome)
                {
                    Intent intent=new Intent(getApplicationContext(),student_dashboard.class);
                    startActivity(intent);
                    finish();
                    return true;

                }
                else if (itemId == R.id.sprofile)
                {
                    Intent intent=new Intent(getApplicationContext(),student_profile.class);
                    startActivity(intent);
                    finish();
                    return true;

                }
                else
                {
                    return false; // No valid menu item selected
                }
            }
        });
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    password.setError("Password must be at least 6 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(user.getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve name, studentId, and class_name from Firestore
                    String name1 = document.getString("name");
                    name.setText(name1);
                    email.setText(user.getEmail());
                }
            }
        });

        update_password = findViewById(R.id.update_password);
        update_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = password.getText().toString().trim(); // Get the new password input

                // Check if the new password is empty
                if (newPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the current user from Firebase Auth
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                // Ensure the user is logged in
                if (currentUser != null) {
                    // Update the user's password
                    currentUser.updatePassword(newPassword)
                            .addOnCompleteListener( task -> {
                                if (task.isSuccessful()) {
                                    // Password updated successfully
                                    Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If password update failed
                                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // If no user is logged in
                    Toast.makeText(getApplicationContext(), "No user is logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}