package com.example.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // FIXED: correct Toolbar import
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView welcomeText;

    private ImageView quoteImageView;
    private DatabaseReference quotesRef;
    private List<String> quoteUrls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeText = findViewById(R.id.welcome_text);


        quoteImageView = findViewById(R.id.quoteImageView);

        quoteUrls = new ArrayList<>();
        quotesRef = FirebaseDatabase.getInstance().getReference("quotes");


        TextView fakeSearchBar = findViewById(R.id.fakeSearchBar);
        fakeSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CourseSearchActivity.class);
                startActivity(intent);
            }
        });



        //GET STORAGE PERMISSION
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is signed in
        if (mAuth.getCurrentUser() == null) {
            // If not signed in, redirect to the Sign-In Activity
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
            return;
        }

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up Drawer Toggle (hamburger icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fetchRandomQuote();

        // Handle Navigation Drawer item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_search) {
                showToast("Search selected");
            } else if (id == R.id.nav_profile) {
                showToast("Profile selected");
            } else if (id == R.id.nav_upload) {
                showToast("Upload selected");
            } else if (id == R.id.about_us) {
                Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut(); // Sign out from Firebase

                // Also sign out from Google if user used Google Sign-In
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                        this,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.client_id))
                                .requestEmail()
                                .build()
                );

                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    showToast("Logged out successfully");
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish(); // prevent user from coming back with back button
                });
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Handle Bottom Navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_myUpload) {
                Intent intent = new Intent(MainActivity.this, MyUploadsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_upload) {
                Intent intent = new Intent(this, uploadPassyear.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_updates) {
                Intent intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, MainProfile.class);
                startActivity(intent);
                showToast("Bottom: Profile selected");
                return true;
            }

            return false;
        });

        // Check if the user is logging in for the first time
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if user profile is complete
                if (dataSnapshot.exists()) {
                    String contactNumber = dataSnapshot.child("contactNumber").getValue(String.class);
                    String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);

                    if (fullName != null) {
                        welcomeText.setText("Welcome, " + fullName + "!");
                    }

                    // If any of the required fields are missing, redirect to profile completion activity
                    if (contactNumber == null || dateOfBirth == null || fullName == null || gender == null) {
                        startActivity(new Intent(MainActivity.this, CompleteProfileActivity.class));
                        finish();  // Prevent the user from coming back to the MainActivity until profile is completed
                    }
                } else {
                    // If user does not exist in the database, create a new entry
                    startActivity(new Intent(MainActivity.this, CompleteProfileActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void fetchRandomQuote() {
        quotesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quoteUrls.clear();

                if (!snapshot.exists()) {
                    Log.d("RandomQuote", "No data found in Firebase");
                    return;
                }

                for (DataSnapshot quoteSnapshot : snapshot.getChildren()) {
                    String url = quoteSnapshot.getValue(String.class);
                    if (url != null) {
                        quoteUrls.add(url);
                        Log.d("RandomQuote", "Retrieved URL: " + url);  // Log each URL
                    }
                }

                if (!quoteUrls.isEmpty()) {
                    // Pick a random image
                    Random random = new Random();
                    int randomIndex = random.nextInt(quoteUrls.size());
                    String randomUrl = quoteUrls.get(randomIndex);
                    Log.d("RandomQuote", "Selected URL: " + randomUrl);  // Log the randomly selected URL

                    // Load it into the ImageView
                    Glide.with(MainActivity.this)
                            .load(randomUrl)
                            .into(quoteImageView);
                } else {
                    Log.d("RandomQuote", "No URLs found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RandomQuote", "Failed to load quote: " + error.getMessage());
            }
        });
    }





}
