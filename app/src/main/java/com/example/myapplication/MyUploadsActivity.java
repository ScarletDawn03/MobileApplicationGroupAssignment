package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display and manage uploads made by the logged-in user.
 */
public class MyUploadsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;               // RecyclerView to show the list of uploads
    private MyUploadsAdapter adapter;                // Adapter for binding upload items to the RecyclerView
    private List<UploadItem> uploadItemList;         // List to hold upload data
    private DatabaseReference coursesRef;            // Reference to "courses" node in Firebase Realtime Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);

        // Set up the toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize RecyclerView and set layout
        recyclerView = findViewById(R.id.recycler_view_uploads);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize list and adapter
        uploadItemList = new ArrayList<>();
        adapter = new MyUploadsAdapter(uploadItemList, this);
        recyclerView.setAdapter(adapter);

        // Reference to Firebase "courses" node
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Load uploads created by the logged-in user
        loadMyUploads();
    }

    /**
     * Loads the current user's uploaded documents from Firebase.
     */
    private void loadMyUploads() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", null);

        if (userEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch course documents from Firebase where the creator matches the logged-in user
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploadItemList.clear(); // Clear existing list before loading new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UploadItem item = dataSnapshot.getValue(UploadItem.class);
                    if (item != null && userEmail.equals(item.getCreated_by())) {
                        uploadItemList.add(item); // Add only user's own uploads
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh RecyclerView with updated data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyUploadsActivity.this, "Failed to load uploads.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes an uploaded document both from Firebase Storage and Realtime Database.
     *
     * @param uploadItem The item to delete
     * @param position   The position of the item in the list
     */
    public void deleteUpload(UploadItem uploadItem, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Upload")
                .setMessage("Are you sure you want to delete \"" + uploadItem.getCr_pdfName() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the file from Firebase Storage
                    StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadItem.getCr_pdfUrl());
                    fileRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                // If storage deletion succeeds, delete the reference from the database
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("courses");
                                dbRef.child(uploadItem.getKey()).removeValue()
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Remove from local list and update adapter
                                            uploadItemList.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            adapter.notifyItemRangeChanged(position, uploadItemList.size());
                                            Toast.makeText(this, "Upload deleted successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the back button in the toolbar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
