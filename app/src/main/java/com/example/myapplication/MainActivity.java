package com.example.myapplication;

// Android core components
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// AndroidX components
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

// Third-party libraries
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

// Google Sign-In components
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

// Firebase components
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

// Java utilities
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MainActivity - The primary activity after user authentication.
 * Handles:
 * - Navigation drawer and bottom navigation
 * - User profile verification
 * - Motivational quote display
 * - Total likes counting
 * - Core app navigation functionality
 */
public class MainActivity extends AppCompatActivity {

    // Navigation components
    private BottomNavigationView bottomNavigationView; // Bottom navigation bar
    private NavigationView navigationView; // Side navigation drawer
    private DrawerLayout drawerLayout; // Container for navigation drawer

    // Firebase components
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DatabaseReference databaseReference; // Reference to user data
    private DatabaseReference quotesRef; // Reference to motivational quotes
    private DatabaseReference coursesRef; // Reference to courses data

    // UI components
    private TextView welcomeText; // Welcome message with user's name
    private ImageView quoteImageView; // Displays motivational quotes
    private TextView totalLikesText; // Shows user's total likes count

    // Data containers
    private List<String> quoteUrls; // Stores URLs of motivational quotes

    /**
     * Initializes the activity, sets up UI components and verifies user session.
     * @param savedInstanceState Saved state bundle for activity recreation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        welcomeText = findViewById(R.id.welcome_text);
        totalLikesText = findViewById(R.id.totalLikesText);
        quoteUrls = new ArrayList<>();

        // Request storage permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        quotesRef = FirebaseDatabase.getInstance().getReference("quotes");
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Verify user authentication
        if (mAuth.getCurrentUser() == null) {
            // Redirect to sign-in if not authenticated
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
            return;
        }

        // Initialize toolbar and navigation components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Initialize quote image view with loading animation
        quoteImageView = findViewById(R.id.quoteImageView);
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(quoteImageView);

        // Set up fake search bar functionality
        TextView fakeSearchBar = findViewById(R.id.fakeSearchBar);
        fakeSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CourseSearchActivity.class));
            }
        });

        // Configure drawer toggle (hamburger icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load initial data
        fetchRandomQuote();
        fetchTotalLikes();

        // Set up navigation drawer item click listeners
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemClick(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up bottom navigation item click listeners
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            return handleBottomNavItemClick(item.getItemId());
        });

        // Initialize user data references
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        // Verify and load user profile
        checkUserProfileCompletion();
        populateUserHeader();
    }

    /**
     * Handles back button press behavior for navigation drawer.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Displays a short toast message.
     * @param message The text to display in the toast
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Verifies if user profile is complete, redirects if incomplete.
     */
    private void checkUserProfileCompletion() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get profile fields
                    String contactNumber = dataSnapshot.child("contactNumber").getValue(String.class);
                    String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);

                    // Update welcome text if name exists
                    if (fullName != null) {
                        welcomeText.setText("Welcome, " + fullName + "!");
                    }

                    // Redirect if any required field is missing
                    if (contactNumber == null || dateOfBirth == null ||
                            fullName == null || gender == null) {
                        startActivity(new Intent(MainActivity.this, CompleteProfileActivity.class));
                        finish();
                    }
                } else {
                    // Redirect if profile doesn't exist
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

    /**
     * Fetches and displays a random motivational quote from Firebase.
     */
    private void fetchRandomQuote() {
        quotesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quoteUrls.clear();

                if (!snapshot.exists()) {
                    Log.d("RandomQuote", "No data found in Firebase");
                    return;
                }

                // Collect all quote URLs
                for (DataSnapshot quoteSnapshot : snapshot.getChildren()) {
                    String url = quoteSnapshot.getValue(String.class);
                    if (url != null) {
                        quoteUrls.add(url);
                        Log.d("RandomQuote", "Retrieved URL: " + url);
                    }
                }

                // Display random quote if available
                if (!quoteUrls.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(quoteUrls.size());
                    String randomUrl = quoteUrls.get(randomIndex);
                    Log.d("RandomQuote", "Selected URL: " + randomUrl);

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

    /**
     * Populates navigation header with user profile information.
     */
    private void populateUserHeader() {
        View headerView = navigationView.getHeaderView(0);
        ImageView imgProfile = headerView.findViewById(R.id.photo);
        TextView tvName = headerView.findViewById(R.id.nav_header_title);
        TextView tvEmail = headerView.findViewById(R.id.user_email);

        databaseReference.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Load profile data if available
                String photoUrl = snapshot.child("profilePhotoUrl").getValue(String.class);
                String fullName = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (photoUrl != null) Glide.with(this).load(photoUrl).into(imgProfile);
                if (fullName != null) tvName.setText(fullName);
                if (email != null) tvEmail.setText(email);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Calculates and displays the total likes received by the user's uploads.
     */
    private void fetchTotalLikes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            totalLikesText.setText("Total Likes: 0");
            return;
        }

        String userEmail = currentUser.getEmail();

        // Listen for likes data changes
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalLikes = 0;

                // Sum likes for all user's uploads
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    UploadItem item = courseSnapshot.getValue(UploadItem.class);
                    if (item != null && userEmail.equals(item.getCreated_by())) {
                        totalLikes += item.getLikes();
                    }
                }

                totalLikesText.setText("Total Likes: " + totalLikes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Failed to read likes", error.toException());
            }
        });
    }

    /**
     * Handles navigation drawer item selection.
     * @param itemId The ID of the selected menu item
     */
    private void handleNavigationItemClick(int itemId) {
        if (itemId == R.id.nav_search) {
            startActivity(new Intent(this, CourseSearchActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, MainProfile.class));
        } else if (itemId == R.id.nav_myUpload) {
            startActivity(new Intent(this, MyUploadsActivity.class));
        } else if (itemId == R.id.nav_upload) {
            startActivity(new Intent(this, uploadPassyear.class));
        } else if (itemId == R.id.about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        }
    }

    /**
     * Handles bottom navigation item selection.
     * @param itemId The ID of the selected menu item
     * @return true if item was handled, false otherwise
     */
    private boolean handleBottomNavItemClick(int itemId) {
        if (itemId == R.id.nav_myUpload) {
            startActivity(new Intent(this, MyUploadsActivity.class));
            return true;
        } else if (itemId == R.id.nav_upload) {
            startActivity(new Intent(this, uploadPassyear.class));
            return true;
        } else if (itemId == R.id.nav_updates) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, MainProfile.class));
            return true;
        }
        return false;
    }

    /**
     * Handles user logout process including Firebase and Google sign-out.
     */
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();

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
            finish();
        });
    }
}