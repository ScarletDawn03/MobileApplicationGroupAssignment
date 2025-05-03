package com.example.myapplication;

// Android core components
import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// AndroidX components
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
    private ImageView quoteImageView, heart1,heart2;
    private TextView totalLikesText;; // Shows user's total likes count

    // Data containers
    private List<String> quoteUrls; // Stores URLs of motivational quotes

    private SharedPreferences prefs;
    private long lastMotivationShown = 0;


    // Separate cooldown constants
    private static final long DIALOG_COOLDOWN = 86400000;  // 24 hours of cooldown time
    private static final long NOTIFICATION_COOLDOWN = 900000; // 15 minutes of cooldown time
    private static final String PREF_FIRST_LAUNCH = "first_launch"; // to store value when the user open the app

    private long lastDialogTime = 0; // default value for dialog message
    private long lastNotificationTime = 0; //default value for pop-up notification


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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Notifications permissions check for API level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1002 // Unique request code
            );
        }

        //Pop-up notification initialization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "motivation_channel",
                    "Progress Updates",
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH for pop-up notifications
            );
            channel.setDescription("Achievement progress notifications");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }


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

        // Initialize UI components
        welcomeText = findViewById(R.id.welcome_text);
        totalLikesText = findViewById(R.id.totalLikesText);

        // Measure the text width
        Paint paint = totalLikesText.getPaint();
        String text = totalLikesText.getText().toString();
        float width = paint.measureText(text);

// Create a LinearGradient with rainbow colors
        Shader shader = new LinearGradient(
                0, 0, width, 0,
                new int[]{
                        Color.RED,
                        Color.MAGENTA,
                        Color.BLUE,
                        Color.parseColor("#008080"),
                        Color.GREEN,
                        Color.parseColor("#800080"),
                        Color.RED // loop back to red
                },
                null,
                Shader.TileMode.CLAMP
        );

// Apply shader to text
        totalLikesText.getPaint().setShader(shader);
        totalLikesText.invalidate();


        heart1= findViewById(R.id.heart1);
        heart2= findViewById(R.id.heart2);
        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);
        heart1.startAnimation(blinkAnimation);
        heart2.startAnimation(blinkAnimation);


        // Initialize quote image view with loading animation
        quoteImageView = findViewById(R.id.quoteImageView);
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(quoteImageView);

        // Set initial alpha and scale (optional for visual effect)
        quoteImageView.setAlpha(0f);
        quoteImageView.setScaleX(0.5f);
        quoteImageView.setScaleY(0.5f);
        quoteImageView.setRotation(0f);

// Create animations
        ObjectAnimator rotate = ObjectAnimator.ofFloat(quoteImageView, "rotation", 0f, 1080f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(quoteImageView, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(quoteImageView, "scaleY", 0.5f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(quoteImageView, "alpha", 0f, 1f);

// Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotate, scaleX, scaleY, fadeIn);
        animatorSet.setDuration(2000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();


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
        checkForMotivations();
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

    //function to check for toggle button from notification preferences java code
    private void checkForMotivations() {
        boolean allowDialogs = prefs.getBoolean("pref_show_dialogs", true);
        boolean allowNotifications = prefs.getBoolean("pref_show_notifications", true);

        long now = System.currentTimeMillis();

        // Always show on app launch (first check)
        boolean showDialog = allowDialogs &&
                (now - lastDialogTime) > DIALOG_COOLDOWN;

        boolean showNotification = allowNotifications &&
                (now - lastNotificationTime) > NOTIFICATION_COOLDOWN;

        if (showDialog || showNotification) {
            checkUploadCount(showDialog, showNotification);
        }
    }

    //function to check total upload count
    private void checkUploadCount(boolean showDialog, boolean showNotification) {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int uploadCount = 0;
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    UploadItem item = courseSnap.getValue(UploadItem.class);
                    if (item != null && item.getCreated_by().equals(mAuth.getCurrentUser().getEmail())) {
                        uploadCount++;
                    }
                }

                if (showDialog) showMotivationDialog(uploadCount);
                if (showNotification) showMotivationalNotification(uploadCount);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //function to show motivational dialog
    private void showMotivationDialog(int uploadCount) {
        String message = generateMessage(uploadCount);
        if (message != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Achievement Upload Progress")
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> {
                        // Update DIALOG cooldown only
                        lastDialogTime = System.currentTimeMillis();
                    })
                    .show();
        }
    }

    //function to pop-up motivational notifications
    private void showMotivationalNotification(int uploadCount) {
        String message = generateMessage(uploadCount);
        if (message == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "motivation_channel")
                .setSmallIcon(R.drawable.achievement_icon)
                .setContentTitle("Latest Update Progress")
                .setContentText(message)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            manager.notify((int) System.currentTimeMillis(), builder.build());

            // Update NOTIFICATION cooldown
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    //function to generate the motivation message
    private String generateMessage(int uploadCount) {
        // Test with fixed value
        // uploadCount = 5; // Uncomment for testing

        if (uploadCount < AchievementConstant.SILVER_MIN) {
            int remaining = AchievementConstant.SILVER_MIN - uploadCount;
            return remaining + " more uploads to Silver!";
        } else if (uploadCount < AchievementConstant.GOLD_MIN) {
            int remaining = AchievementConstant.GOLD_MIN - uploadCount;
            return remaining + " more uploads to Gold!";
        } else if (uploadCount < AchievementConstant.MAX_UPLOADS) {
            int remaining = AchievementConstant.MAX_UPLOADS - uploadCount;
            return remaining + " more uploads to Max Tier!";
        }
        return "Keep uploading to unlock achievements!"; // Default message
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