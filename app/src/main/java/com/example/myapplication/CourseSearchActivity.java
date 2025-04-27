package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class CourseSearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private Spinner categorySpinner;
    private RecyclerView recyclerView;
    private List<SourceDocumentModelClass> courseList = new ArrayList<>();
    private CourseAdapter adapter;

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private DatabaseReference documentsRef;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        // Set up the Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);  // Make sure you have a Toolbar with this ID in your layout
        setSupportActionBar(toolbar);

        // Set the back button (home) in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(courseList);
        recyclerView.setAdapter(adapter);

        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        documentsRef = db.getReference("courses");  // Correct reference to 'courses' node

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

}
