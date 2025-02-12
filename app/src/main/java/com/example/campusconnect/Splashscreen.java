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

        // Add a scale animation to make the logo bigger
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        logo.startAnimation(scaleUp);

        // Delay for a few seconds (e.g., 3 seconds) before transitioning to the main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splashscreen.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add a fade-in transition
            finish(); // Close the Splashscreen activity
        }, 3000); // 3000 ms = 3 seconds
    }
}