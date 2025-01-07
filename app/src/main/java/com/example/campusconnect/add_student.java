package com.example.campusconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class add_student extends AppCompatActivity {
    Button student_add,std_list,update_std,delete_std;
    TextView student_username_signup, student_password_signup, student_name,student_no;
    Spinner class_list;
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
        setContentView(R.layout.activity_add_student);
        LoadingDialog loadingDialog = new LoadingDialog(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.addstd);
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

        student_username_signup = findViewById(R.id.student_username_signup);
        student_password_signup = findViewById(R.id.student_password_signup);
        student_name = findViewById(R.id.student_name);
        class_list = findViewById(R.id.class_list);
        student_no=findViewById(R.id.student_no);


        std_list = findViewById(R.id.std_list);
        std_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),show_std.class);
                startActivity(intent);
            }
        });

        student_password_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    student_password_signup.setError("Password must be at least 6 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        student_username_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
                if (!student_username_signup.getText().toString().matches(emailPattern))
                {
                    student_username_signup.setError("Invalid email");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        student_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(student_no.length() ==10))
                {
                    student_no.setError("Invalid Mobile Number");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        student_add = findViewById(R.id.student_signup_btn);
        student_add.setOnClickListener(v -> {
            String email = student_username_signup.getText().toString().trim();
            String password = student_password_signup.getText().toString().trim();
            String name = student_name.getText().toString().trim();
            String mobile_no = student_no.getText().toString().trim();
            String className = class_list.getSelectedItem() != null ? class_list.getSelectedItem().toString().trim() : "";

            // Check for empty fields
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || className.isEmpty() || mobile_no.isEmpty()) {
                Toast.makeText(this, "Enter All Details", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("EmailValidation", "Starting email validation...");
            // Email validation
            String emailPattern = "[a-z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}";
            if (!email.matches(emailPattern)) {
                Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("EmailValidation", "Valid Email: " + email);

            if (password.length()<6)
            {
                Toast.makeText(getApplicationContext(), "Invalid Password ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mobile_no.length()!=10)
            {
                Toast.makeText(getApplicationContext(), "Invalid Number ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show the dialog
            loadingDialog.show();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.runTransaction(transaction -> {
                // Get the current value of the sequence
                DocumentReference sequenceDoc = db.collection("sequences").document("students");
                DocumentSnapshot snapshot = transaction.get(sequenceDoc);

                long currentId = snapshot.exists() ? snapshot.getLong("current_id") : 0;
                long newId = currentId + 1;

                // Update the sequence
                transaction.update(sequenceDoc, "current_id", newId);

                return newId;
            }).addOnSuccessListener(newStdId -> {
                // Use the generated std_id to create the student

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getEmail();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", student_name.getText().toString());
                        userData.put("stdid", newStdId); // System-generated std_id
                        userData.put("class", class_list.getSelectedItem().toString());
                        userData.put("email", email);
                        userData.put("mobile_no", mobile_no);
                        userData.put("role", "student");

                        db.collection("users").document(student_username_signup.getText().toString()).set(userData).addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();

                            // Restore teacher session
                            SharedPreferences prefs = getSharedPreferences("CampusConnectPrefs", MODE_PRIVATE);
                            String teacherEmail = prefs.getString("teacherEmail", null);
                            String teacherPassword = prefs.getString("teacherPassword", null);

                            // Perform your login task here, and dismiss the dialog when done
                            auth.signOut();

                            if (teacherEmail != null && teacherPassword != null)
                            {
                                auth.signInWithEmailAndPassword(teacherEmail, teacherPassword).addOnCompleteListener(signInTask ->
                                {
                                    if (signInTask.isSuccessful())
                                    {
                                        // Perform your login task here, and dismiss the dialog when done
                                        loadingDialog.dismiss();
                                        Toast.makeText(this, "Switched back to teacher account.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, add_student.class));
                                    }
                                    else
                                    {
                                        // Perform your login task here, and dismiss the dialog when done
                                        loadingDialog.dismiss();
                                        Toast.makeText(this, "Failed to log back into teacher account.", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                });
                            }
                            else
                            {
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Teacher session not found. Please log in again.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, teacher_login.class));
                                finish();
                            }
                        });
                    }
                });
            }).addOnFailureListener(e -> {
                // Perform your login task here, and dismiss the dialog when done
                loadingDialog.dismiss();
                Toast.makeText(this, "Failed to generate std_id. Try again.", Toast.LENGTH_SHORT).show();
            });
        });

        update_std=findViewById(R.id.update_std);
        update_std.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),update_std.class);
                startActivity(intent);

            }
        });

        delete_std=findViewById(R.id.delete_std);
        delete_std.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),delete_std.class);
                startActivity(intent);

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