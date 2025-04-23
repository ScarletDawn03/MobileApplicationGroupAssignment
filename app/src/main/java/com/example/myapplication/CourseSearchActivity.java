package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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

        searchView = findViewById(R.id.search_view);
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
            String searchQuery = searchView.getQuery().toString().trim();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            // Add debug toast here
            Toast.makeText(this, "Searching: " + searchQuery + " in " + selectedCategory, Toast.LENGTH_SHORT).show();

            if (searchQuery.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(CourseSearchActivity.this, "Please fill in the search field and select a category", Toast.LENGTH_SHORT).show();
            } else {
                fetchDocuments(searchQuery, selectedCategory);
            }
        });
    }

    private void fetchDocuments(String courseCode, String category) {
        // Query Firebase Database to get documents matching courseCode
        documentsRef.orderByChild("cr_code")
                .equalTo(courseCode)
                .get()
                .addOnSuccessListener(dataSnapshot -> {

                    Log.d("FirebaseDebug", "Querying for courseCode: " + courseCode + ", Category: " + category);
                    Log.d("FirebaseDebug", "Found: " + dataSnapshot.getChildrenCount() + " items");

                    courseList.clear(); // Clear the list before adding new items

                    for (DataSnapshot docSnapshot : dataSnapshot.getChildren()) {
                        SourceDocumentModelClass item = docSnapshot.getValue(SourceDocumentModelClass.class);

                        if (item != null && item.getCr_pdfName() != null) {
                            String fileName = item.getCr_pdfName();
                            StorageReference fileRef = storage.getReference().child("pdfs/" + fileName);

                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Update the model with the file URL
                                item.setCr_pdfUrl(uri.toString());
                                courseList.add(item);  // Add the item to the list
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    // Notify the adapter once all items have been added
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
