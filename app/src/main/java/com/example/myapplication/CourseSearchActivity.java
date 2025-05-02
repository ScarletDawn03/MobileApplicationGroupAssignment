package com.example.myapplication;

// Android core components
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

// AndroidX components
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase components
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// Java utilities
import java.util.ArrayList;
import java.util.List;

// Toolbar and menu items
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

// Notification imports
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// Permission handling
import androidx.core.content.ContextCompat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import android.Manifest;
import android.content.pm.PackageManager;

/**
 * Activity for searching courses and displaying results.
 * Handles course search functionality, notifications for likes, and document display.
 */
public class CourseSearchActivity extends AppCompatActivity {

    // UI components
    private SearchView searchView;  // Search input field
    private Spinner categorySpinner; // Category selection dropdown
    private RecyclerView recyclerView; // List to display search results
    private List<SourceDocumentModelClass> courseList; // List of courses to display
    private CourseAdapter adapter; // Adapter for the RecyclerView

    // Firebase components
    private FirebaseDatabase db; // Firebase Realtime Database instance
    private FirebaseStorage storage; // Firebase Storage instance
    private DatabaseReference documentsRef; // Reference to 'courses' node in database
    private Button searchButton; // Button to trigger search

    // Notification permission request code
    private static final int REQ_POST_NOTIFICATIONS = 1001;  // Unique request code for notification permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notification channel creation (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "likes_channel", // Channel ID
                    "Likes & Comments", // User-visible name
                    NotificationManager.IMPORTANCE_HIGH // Importance level
            );
            channel.setDescription("Notifications when you like or someone likes your documents");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIFICATIONS
                );
            }
        }

        setContentView(R.layout.activity_course_search);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize course list
        courseList = new ArrayList<>();

        // Initialize search view with hint and input limitation
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("Enter Course Code");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Let the search button handle submission
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Limit input to 10 characters
                if (newText.length() > 10) {
                    searchView.setQuery(newText.substring(0, 10), false);
                    Toast.makeText(CourseSearchActivity.this, "Max 10 characters allowed", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        // Initialize UI components
        categorySpinner = findViewById(R.id.upload_category);
        recyclerView = findViewById(R.id.recycler_view);
        searchButton = findViewById(R.id.search_course_button);

        // Get current user info
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Initialize Firebase components
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        documentsRef = db.getReference("courses"); // Reference to courses in database

        // Set up listener for likes on user's uploads
        documentsRef.orderByChild("created_by").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        for (DataSnapshot courseSnap : snap.getChildren()) {
                            String courseName = courseSnap.child("cr_name").getValue(String.class);
                            String key = courseSnap.getKey();
                            documentsRef.child(key).child("liked_by")
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override public void onChildAdded(DataSnapshot likeSnap, String prev) {
                                            String likerUid = likeSnap.getKey();
                                            // Skip notifications for user's own likes
                                            if (!likerUid.equals(userEmail)){
                                                // Check user preference for notifications
                                                boolean allowLikes = PreferenceManager
                                                        .getDefaultSharedPreferences(CourseSearchActivity.this)
                                                        .getBoolean("pref_notify_likes_comments", true);
                                                if (allowLikes) {
                                                    // Build and show notification
                                                    NotificationCompat.Builder notif = new NotificationCompat.Builder(CourseSearchActivity.this, "likes_channel")
                                                            .setSmallIcon(R.drawable.favourite_icon)
                                                            .setContentTitle("Someone liked your document")
                                                            .setContentText(likerUid + " liked \"" + courseName + "\"")
                                                            .setAutoCancel(true);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        if (ContextCompat.checkSelfPermission(CourseSearchActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                                                                == PackageManager.PERMISSION_GRANTED) {
                                                            NotificationManagerCompat.from(CourseSearchActivity.this)
                                                                    .notify((int) System.currentTimeMillis(), notif.build());
                                                        } else {
                                                            // Request permission if not granted
                                                            ActivityCompat.requestPermissions(
                                                                    CourseSearchActivity.this,
                                                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                                    REQ_POST_NOTIFICATIONS
                                                            );
                                                        }
                                                    } else {
                                                        // No permission needed pre-Android 13
                                                        NotificationManagerCompat.from(CourseSearchActivity.this)
                                                                .notify((int) System.currentTimeMillis(), notif.build());
                                                    }
                                                }
                                            }
                                        }
                                        @Override public void onChildChanged(DataSnapshot s, String p) {}
                                        @Override public void onChildRemoved(DataSnapshot s) {}
                                        @Override public void onChildMoved(DataSnapshot s, String p) {}
                                        @Override public void onCancelled(DatabaseError e) {}
                                    });
                        }
                    }
                    @Override public void onCancelled(DatabaseError e) { }
                });

        // Set up RecyclerView with adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(this, courseList, documentsRef, userEmail);
        recyclerView.setAdapter(adapter);

        // Search button click handler
        searchButton.setOnClickListener(v -> {
            // Process search query (trim, remove spaces, uppercase)
            String searchQuery = searchView.getQuery().toString().trim().replaceAll("\\s+", "").toUpperCase();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            // Validate input
            if (searchQuery.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(CourseSearchActivity.this, "Please fill in the search field and select a category", Toast.LENGTH_SHORT).show();
            } else {
                fetchDocuments(searchQuery, selectedCategory); // Perform search
            }
        });

        // Enable back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Fetches documents from Firebase based on course code and category.
     * @param courseCode The course code to search for
     * @param category The document category to filter by
     */
    private void fetchDocuments(String courseCode, String category) {
        documentsRef.orderByChild("cr_code")
                .equalTo(courseCode)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    // Log search parameters and results
                    Log.d("FirebaseDebug", "Querying for courseCode: " + courseCode + ", Category: " + category);
                    Log.d("FirebaseDebug", "Found: " + dataSnapshot.getChildrenCount() + " items");

                    courseList.clear(); // Clear previous results

                    boolean found = false; // Track if matching documents are found

                    // Process each matching document
                    for (DataSnapshot docSnapshot : dataSnapshot.getChildren()) {
                        SourceDocumentModelClass item = docSnapshot.getValue(SourceDocumentModelClass.class);

                        if (item != null && item.getCr_pdfName() != null) {
                            String fileName = item.getCr_pdfName();

                            // Check category match
                            if (item.getCr_category() != null && item.getCr_category().equalsIgnoreCase(category)) {
                                // Get download URL from Firebase Storage
                                StorageReference fileRef = storage.getReference().child("pdfs/" + fileName);
                                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    item.setCr_pdfUrl(uri.toString());
                                    courseList.add(item);
                                    adapter.notifyDataSetChanged(); // Update UI
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to fetch file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                found = true; // Mark as found
                            }
                        }
                    }

                    // Show "not found" dialog if no matches
                    if (!found) {
                        new AlertDialog.Builder(this)
                                .setTitle("Not Found")
                                .setMessage("No course found for the given code and category.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    resetPage(); // Reset UI
                                })
                                .setCancelable(false)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Resets the search page to initial state.
     * Clears search text, resets spinner, and clears results.
     */
    private void resetPage() {
        searchView.setQuery("", false); // Clear search text
        categorySpinner.setSelection(0); // Reset spinner
        courseList.clear(); // Clear results
        adapter.notifyDataSetChanged(); // Update UI
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles permission request results.
     * @param requestCode The request code passed in requestPermissions()
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}