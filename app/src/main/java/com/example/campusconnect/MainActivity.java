package com.example.campusconnect;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    Button gotote,gotostd,teach_signup_btn;
    FirebaseAuth auth=FirebaseAuth.getInstance();
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
        LoadingDialog loadingDialog = new LoadingDialog(this);
        // Check if a user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
        {
            // Show the dialog
            loadingDialog.show();
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
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
                                finish(); // Close the current activity
                            }
                            else if ("teacher".equals(role))
                            {
                                // Redirect to Teacher Dashboard
                                Intent i = new Intent(getApplicationContext(), teacher_dashboard.class);
                                startActivity(i);
                                // Perform your login task here, and dismiss the dialog when done
                                loadingDialog.dismiss();
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
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gotote=findViewById(R.id.gotote);
        gotote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, teacher_login.class);
                startActivity(intent);

            }
        });
        gotostd=findViewById(R.id.gotostd);
        gotostd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MainActivity.this, student_login.class);
                startActivity(intent);


            }
        });
        teach_signup_btn=findViewById(R.id.teach_signup_btn);
        teach_signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MainActivity.this, techer_signup.class);
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