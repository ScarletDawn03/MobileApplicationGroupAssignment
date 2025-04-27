package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainProfile extends AppCompatActivity {
    private Button btnGoToProfile, btnThemeSettings, btnNotificationPref,btnPrivacySettings,btnAchievements;
    private TextView tvName, tvHandle;

    private String userId;

    private ImageView imgProfile;
    private DatabaseReference dbRef;
    private StorageReference storageRef;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_interface);

        //For API 26++
        // call once in Application.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("App notifications");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // Request permission at runtime (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Profile Information
        View rowProfile = findViewById(R.id.menu_profile_information);
        ((TextView)rowProfile.findViewById(R.id.tvMenuLabel))
                .setText("Profile Information");

        // Change Theme
        View rowTheme = findViewById(R.id.menu_theme);
        ((TextView)rowTheme.findViewById(R.id.tvMenuLabel))
                .setText("Change Theme");

        // Notification Preferences
        View rowNotif = findViewById(R.id.menu_notifications);
        ((TextView)rowNotif.findViewById(R.id.tvMenuLabel))
                .setText("Notification Preferences");

        // Privacy Settings
        View rowPrivacy = findViewById(R.id.menu_privacy);
        ((TextView)rowPrivacy.findViewById(R.id.tvMenuLabel))
                .setText("Privacy Settings");

        // My Achievements
        View rowAch = findViewById(R.id.menu_achievements);
        ((TextView)rowAch.findViewById(R.id.tvMenuLabel))
                .setText("My Achievements");

        ImageView imgProfile = findViewById(R.id.imgAvatar);
        TextView tvName   = findViewById(R.id.tvName);
        TextView tvHandle = findViewById(R.id.tvHandle);

        // Determine user ID dynamically
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "U001"; // Test ID fallback
            Toast.makeText(this, "User not signed in, using test ID", Toast.LENGTH_SHORT).show();
        }

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_photos");

        // Load profile photo and data
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String photoUrl = snapshot.child("profilePhotoUrl").getValue(String.class);
                if (photoUrl != null) Glide.with(this).load(photoUrl).into(imgProfile);

                tvName.setText(snapshot.child("fullName").getValue(String.class));
                tvHandle.setText("@" + snapshot.child("username").getValue(String.class));


            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );



        // Fetch FCM Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "Manual Token: " + token);
                });


           /* btnGoToProfile = findViewById(R.id.menu_profile_information);
            btnThemeSettings = findViewById(R.id.menu_theme);
            btnNotificationPref = findViewById(R.id.menu_notifications);
            btnPrivacySettings = findViewById(R.id.menu_privacy);
            btnAchievements = findViewById(R.id.menu_achievements);*/

        rowProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainProfile.this, ProfileActivity.class);
            startActivity(intent);
        });

        rowTheme.setOnClickListener(v -> {
            Intent intent = new Intent(MainProfile.this, AppThemeSettings.class);
            startActivity(intent);
        });

        rowNotif.setOnClickListener(v -> {
            Intent intent = new Intent(MainProfile.this, NotificationPreferences.class);
            startActivity(intent);
        });

        rowPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(MainProfile.this, PrivacySettings.class);
            startActivity(intent);
        });

        rowAch.setOnClickListener(v -> {
            Intent intent = new Intent(MainProfile.this, AchievementActivity.class);
            startActivity(intent);
        });

       /* //Testing for notification features work or not
        Button testBtn = findViewById(R.id.btnTestNotification);
        testBtn.setOnClickListener(v -> {
            MyFirebaseMessagingService.simulateNotification(
                    this,
                    //new MyFirebaseMessagingService(), // simulated context
                    "like_comment",
                    "You got a new like!",
                    "Alice liked your past year paper."
            );
        });*/


    }
}
