package com.example.myapplication;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

/**
 * Activity to display a splash screen when the app starts.
 * This is the introductory screen before navigating to the sign-in page.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets up the splash screen and navigates to the sign-in screen after a delay.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set the splash screen layout

        // Handler to delay the transition to the next activity (SignInActivity)
        // This will wait 2 seconds before starting the SignInActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create an Intent to navigate from SplashActivity to SignInActivity
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(intent); // Start the SignInActivity
            finish(); // Close the current SplashActivity so it doesn't remain in the back stack
        }, 2000); // 2000 milliseconds (2 seconds) delay before transitioning
    }
}
