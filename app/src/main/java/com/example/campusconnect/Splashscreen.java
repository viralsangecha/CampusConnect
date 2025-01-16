package com.example.campusconnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Find the logo view
        ImageView logo = findViewById(R.id.logo);

        // Load and start the fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        // Delay for a few seconds (e.g., 3 seconds) before transitioning to the main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splashscreen.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the Splashscreen activity
        }, 2000); // 3000 ms = 3 seconds
    }
}
