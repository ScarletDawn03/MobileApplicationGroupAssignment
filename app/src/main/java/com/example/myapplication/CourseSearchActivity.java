package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

//For notification
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.core.content.ContextCompat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import android.Manifest;
import android.content.pm.PackageManager;


public class CourseSearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private Spinner categorySpinner;
    private RecyclerView recyclerView;
    private List<SourceDocumentModelClass> courseList;
    private CourseAdapter adapter;

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private DatabaseReference documentsRef;
    private Button searchButton;

    //For notification like
    private static final int REQ_POST_NOTIFICATIONS = 1001;  // any unique int


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For notification appear on the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "likes_channel",
                    "Likes & Comments",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications when you like or someone likes your documents");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);

        }

        // 2) On Android 13+, prompt for the runtime permission if needed
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

        // Set up the Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);  // Make sure you have a Toolbar with this ID in your layout
        setSupportActionBar(toolbar);

        // Set the back button (home) in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        courseList = new ArrayList<>();


        searchView = findViewById(R.id.search_view);
        // Set the placeholder (hint text) programmatically
        searchView.setQueryHint("Enter Course Code");

        // Set up TextWatcher to limit the input
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 10) {  // Change 10 to any limit you prefer
                    // Trim the text to 10 characters
                    searchView.setQuery(newText.substring(0, 10), false);
                    Toast.makeText(CourseSearchActivity.this, "Max 10 characters allowed", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        categorySpinner = findViewById(R.id.upload_category);
        recyclerView = findViewById(R.id.recycler_view);
        searchButton = findViewById(R.id.search_course_button);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        //To get current userid
        String likerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        documentsRef = db.getReference("courses");  // Correct reference to 'courses' node

        //Function for other people liking YOUR uploads:
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
                                            // Skip your own like:
                                            if (!likerUid.equals(userEmail)){


                                                boolean allowLikes = PreferenceManager
                                                        .getDefaultSharedPreferences(CourseSearchActivity.this)
                                                        .getBoolean("pref_notify_likes_comments", true);
                                                if (allowLikes) {

                                                    // 3 Build and fire the notification
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
                                                            // Optional: ask for it here if you want:
                                                            ActivityCompat.requestPermissions(
                                                                    CourseSearchActivity.this,
                                                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                                    REQ_POST_NOTIFICATIONS
                                                            );
                                                        }
                                                    } else {
                                                        // Pre-Android-13: no runtime permission needed
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(courseList, documentsRef, userEmail);
        recyclerView.setAdapter(adapter);


        // Set up the search button click listener
        searchButton.setOnClickListener(v -> {
            String searchQuery = searchView.getQuery().toString().trim().replaceAll("\\s+", "").toUpperCase();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            if (searchQuery.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(CourseSearchActivity.this, "Please fill in the search field and select a category", Toast.LENGTH_SHORT).show();
            } else {
                fetchDocuments(searchQuery, selectedCategory);
            }
        });

        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void fetchDocuments(String courseCode, String category) {
        documentsRef.orderByChild("cr_code")
                .equalTo(courseCode)
                .get()
                .addOnSuccessListener(dataSnapshot -> {

                    Log.d("FirebaseDebug", "Querying for courseCode: " + courseCode + ", Category: " + category);
                    Log.d("FirebaseDebug", "Found: " + dataSnapshot.getChildrenCount() + " items");

                    courseList.clear(); // Clear the list before adding new items

                    boolean found = false; // Track if we found any matching document

                    for (DataSnapshot docSnapshot : dataSnapshot.getChildren()) {
                        SourceDocumentModelClass item = docSnapshot.getValue(SourceDocumentModelClass.class);

                        if (item != null && item.getCr_pdfName() != null) {
                            String fileName = item.getCr_pdfName();

                            // Check if category matches too
                            if (item.getCr_category() != null && item.getCr_category().equalsIgnoreCase(category)) {
                                StorageReference fileRef = storage.getReference().child("pdfs/" + fileName);

                                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    item.setCr_pdfUrl(uri.toString());
                                    courseList.add(item);
                                    adapter.notifyDataSetChanged();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to fetch file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                found = true; // Found a matching item!
                            }
                        }
                    }

                    if (!found) {  // <-- Notice: checking `found`, not just children count
                        new AlertDialog.Builder(this)
                                .setTitle("Not Found")
                                .setMessage("No course found for the given code and category.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    resetPage(); // Reset after user clicks OK
                                })
                                .setCancelable(false)
                                .show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void resetPage() {
        // Clear the search text
        searchView.setQuery("", false);

        // Reset the Spinner to first item
        categorySpinner.setSelection(0);

        // Clear the RecyclerView list
        courseList.clear();
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check if the item is the "home" button (back button)
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to the main menu or previous activity
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void likeDocument(String documentId, String userEmail) {

        //  Find the matching course in your in-memory list:
        final String courseName;
        String tempName = "";
        for (SourceDocumentModelClass c : courseList) {
            if (c.getKey().equals(documentId)) {
                tempName = c.getCr_name();
                break;
            }
        }

        courseName=tempName;

        DatabaseReference documentRef = documentsRef.child(documentId);

        documentRef.child("liked_by").child(userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(CourseSearchActivity.this, "You have already liked this document", Toast.LENGTH_SHORT).show();
                } else {
                    documentRef.child("likes").get().addOnCompleteListener(likeTask -> {
                        if (likeTask.isSuccessful()) {
                            Integer currentLikes = likeTask.getResult().getValue(Integer.class);
                            if (currentLikes == null) {
                                currentLikes = 0;
                            }
                            documentRef.child("likes").setValue(currentLikes + 1);

                            for (int i = 0; i < courseList.size(); i++) {
                                SourceDocumentModelClass course = courseList.get(i);
                                if (course.getKey().equals(documentId)) {
                                    course.setLikes(currentLikes + 1);
                                    adapter.notifyItemChanged(i);
                                    break;
                                }
                            }

                            documentRef.child("liked_by").child(userEmail).setValue(true)
                                    .addOnCompleteListener(likeUpdateTask -> {
                                        if (likeUpdateTask.isSuccessful()) {
                                            Toast.makeText(CourseSearchActivity.this, "You liked this document!", Toast.LENGTH_SHORT).show();

                                            // 2: Read the user’s setting:
                                            boolean allowLikes = PreferenceManager
                                                    .getDefaultSharedPreferences(CourseSearchActivity.this)
                                                    .getBoolean("pref_notify_likes_comments", true);

                                            // 3: Only fire if they opted in:
                                            if (allowLikes) {
                                                NotificationCompat.Builder notif = new NotificationCompat.Builder(CourseSearchActivity.this, "likes_channel")
                                                        .setSmallIcon(R.drawable.favourite_icon)
                                                        .setContentTitle("You liked a document")
                                                        .setContentText("You liked \"" + courseName + "\"")
                                                        .setAutoCancel(true);

                                                // 4: Permission-wrapped notify
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    if (ContextCompat.checkSelfPermission(
                                                            CourseSearchActivity.this,
                                                            Manifest.permission.POST_NOTIFICATIONS
                                                    ) == PackageManager.PERMISSION_GRANTED) {
                                                        NotificationManagerCompat.from(CourseSearchActivity.this)
                                                                .notify((int) System.currentTimeMillis(), notif.build());
                                                    } else {
                                                        ActivityCompat.requestPermissions(
                                                                CourseSearchActivity.this,
                                                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                                REQ_POST_NOTIFICATIONS
                                                        );
                                                    }
                                                } else {
                                                    NotificationManagerCompat.from(CourseSearchActivity.this)
                                                            .notify((int) System.currentTimeMillis(), notif.build());
                                                }
                                            }
                                        } else {
                                            Toast.makeText(CourseSearchActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }

//Function to verify permission request
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
