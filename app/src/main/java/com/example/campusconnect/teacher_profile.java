package com.example.campusconnect;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class teacher_profile extends AppCompatActivity {

    TextView tname,temail;
    EditText tpassword;
    Button update_tpassword;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

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
        setContentView(R.layout.activity_teacher_profile);
        LoadingDialog loadingDialog = new LoadingDialog(this);

        tname=findViewById(R.id.tname);
        temail=findViewById(R.id.temail);
        tpassword=findViewById(R.id.tpassword);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(user.getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve name, studentId, and class_name from Firestore
                    String name1 = document.getString("name");
                    tname.setText(name1);
                    temail.setText(user.getEmail());
                }
            }
        });

        tpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    tpassword.setError("Password must be at least 6 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        update_tpassword = findViewById(R.id.update_tpassword);
        update_tpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = tpassword.getText().toString().trim(); // Get the new password input

                // Check if the new password is empty
                if (newPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingDialog.show();
                // Get the current user from Firebase Auth
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                // Ensure the user is logged in
                if (currentUser != null) {
                    // Update the user's password
                    currentUser.updatePassword(newPassword)
                            .addOnCompleteListener( task -> {
                                if (task.isSuccessful()) {
                                    // Password updated successfully
                                    // Perform your login task here, and dismiss the dialog when done
                                    loadingDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Perform your login task here, and dismiss the dialog when done
                                    loadingDialog.dismiss();
                                    // If password update failed
                                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // If no user is logged in
                    // Perform your login task here, and dismiss the dialog when done
                    loadingDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No user is logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.tprofile);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.thome)
                {
                    Intent intent=new Intent(getApplicationContext(), teacher_dashboard.class);
                    startActivity(intent);
                    finish();
                    return true;

                }
                else if (itemId == R.id.addstd)
                {
                    Intent intent=new Intent(getApplicationContext(), add_student.class);
                    startActivity(intent);
                    finish();
                    return true;

                }
                else if (itemId == R.id.tprofile)
                {
                    Intent intent=new Intent(getApplicationContext(),teacher_profile.class);
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
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}