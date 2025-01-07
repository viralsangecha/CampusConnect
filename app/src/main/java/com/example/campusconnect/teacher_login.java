package com.example.campusconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class teacher_login extends AppCompatActivity {

    EditText teacher_username_login, Teacher_password_login;
    Button teacher_login_btn;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onStart() {
        super.onStart();
        if (!isNetworkAvailable()) {
            // Redirect to No Internet Activity
            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // Check if a user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getEmail();

            // Fetch the user's role from Firestore
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists())
                        {
                            String role = documentSnapshot.getString("role");
                            if ("student".equals(role))
                            {
                                // Redirect to Student Dashboard
                                Intent i = new Intent(getApplicationContext(), student_dashboard.class);
                                startActivity(i);
                                finish(); // Close the current activity
                            }
                            else if ("teacher".equals(role))
                            {
                                // Redirect to Teacher Dashboard
                                Intent i = new Intent(getApplicationContext(), teacher_dashboard.class);
                                startActivity(i);
                                finish(); // Close the current activity
                            }
                            else
                            {
                                // Handle invalid roles (optional)
                                Toast.makeText(getApplicationContext(), "Invalid role detected", Toast.LENGTH_SHORT).show();
                                auth.signOut(); // Log the user out
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_login);

        LoadingDialog loadingDialog = new LoadingDialog(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        teacher_username_login = findViewById(R.id.teacher_username_login);
        Teacher_password_login = findViewById(R.id.teacher_password_login);
        teacher_login_btn = findViewById(R.id.teacher_login_btn);

        Teacher_password_login.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    Teacher_password_login.setError("Password must be at least 6 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        teacher_username_login.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!teacher_username_login.getText().toString().matches(emailPattern))
                {
                    teacher_username_login.setError("Invalid email");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        teacher_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = teacher_username_login.getText().toString().trim();
                String password = Teacher_password_login.getText().toString().trim();

                // Validate input fields
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(teacher_login.this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!email.matches(emailPattern))
                {
                    Toast.makeText(getApplicationContext(), "Invalid Email ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length()<6)
                {
                    Toast.makeText(getApplicationContext(), "Invalid Email ", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Show the dialog
                loadingDialog.show();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Authenticate the user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Get the authenticated user's UID
                                String uid = auth.getCurrentUser().getEmail();

                                // Fetch the user's role from Firestore
                                DocumentReference userRef = db.collection("users").document(uid);
                                userRef.get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists())
                                            {
                                                String role = documentSnapshot.getString("role");
                                                if ("teacher".equals(role))
                                                {
                                                    // Redirect to Teacher Dashboard
                                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                                    // Save teacher credentials on login
                                                    SharedPreferences sharedPreferences = getSharedPreferences("CampusConnectPrefs", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("teacherEmail", teacher_username_login.getText().toString());
                                                    editor.putString("teacherPassword",Teacher_password_login.getText().toString());
                                                    editor.apply();

                                                    Intent i = new Intent(getApplicationContext(), teacher_dashboard.class);
                                                    i.putExtra("teacher_password",Teacher_password_login.getText().toString());
                                                    startActivity(i);
                                                    // Perform your login task here, and dismiss the dialog when done
                                                    loadingDialog.dismiss();
                                                    finish(); // Close the login activity
                                                }
                                                else if ("student".equals(role))
                                                {
                                                    // Perform your login task here, and dismiss the dialog when done
                                                    loadingDialog.dismiss();
                                                    // User is not authorized as teacher
                                                    Toast.makeText(getApplicationContext(), "user not found or wrong password/username", Toast.LENGTH_SHORT).show();

                                                }
                                                else
                                                {
                                                    // Perform your login task here, and dismiss the dialog when done
                                                    loadingDialog.dismiss();
                                                    // Handle invalid role
                                                    Toast.makeText(getApplicationContext(), "Invalid role detected", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else {
                                                // Perform your login task here, and dismiss the dialog when done
                                                loadingDialog.dismiss();
                                                // Handle case when user data is not found
                                                Toast.makeText(getApplicationContext(), "User data not found ", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Perform your login task here, and dismiss the dialog when done
                                            loadingDialog.dismiss();
                                            // Handle Firestore access failure
                                            Toast.makeText(getApplicationContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                // Handle authentication failure
                                Toast.makeText(getApplicationContext(), "Authentication Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
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