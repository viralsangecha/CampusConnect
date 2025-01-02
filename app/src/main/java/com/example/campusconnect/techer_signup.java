package com.example.campusconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.HashMap;
import java.util.Map;

public class techer_signup extends AppCompatActivity {

    EditText teacher_username_signup,teacher_password_signup,teacher_name;
    Spinner depart_list;
    Button teach_signup;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    @Override
    public void onStart() {
        super.onStart();
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
        setContentView(R.layout.activity_techer_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        teacher_username_signup=findViewById(R.id.teacher_username_signup);
        teacher_password_signup=findViewById(R.id.teacher_password_signup);
        teacher_name=findViewById(R.id.teacher_name);

        teacher_password_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    teacher_password_signup.setError("Password must be at least 6 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        teacher_username_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!teacher_username_signup.getText().toString().matches(emailPattern))
                {
                    teacher_username_signup.setError("Invalid email");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        teach_signup=findViewById(R.id.teacher_signup_btn);
        teach_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password;
                email=teacher_username_signup.getText().toString();
                password=teacher_password_signup.getText().toString();
                if (email.isEmpty() || password.isEmpty() || teacher_name.getText().toString().isEmpty())
                {
                    Toast.makeText(techer_signup.this, "Enter Email or password", Toast.LENGTH_SHORT).show();
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

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getEmail();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name",teacher_name.getText().toString() );
                                userData.put("email", email);
                                userData.put("role","teacher");


                                db.collection("users").document(teacher_username_signup.getText().toString())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(techer_signup.this, "Account created", Toast.LENGTH_SHORT).show();
                                            Intent i=new Intent(getApplicationContext(), teacher_dashboard.class);
                                            startActivity(i);
                                            finish();
                                        });
                            }
                        });

            }
        });

    }
}