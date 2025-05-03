package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // in milliseconds (2.5 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Load animations
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        // Hook views
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_text);

        // Set animations
        logo.setAnimation(topAnim);
        appName.setAnimation(bottomAnim);

        // Delay before switching to main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
