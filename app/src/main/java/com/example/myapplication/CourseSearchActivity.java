package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search); // Replace with your layout name

        searchView = findViewById(R.id.search_view);
        categorySpinner = findViewById(R.id.upload_category);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(courseList);
        recyclerView.setAdapter(adapter);

        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        documentsRef = db.getReference("documents"); // Reference to "documents" node in Firebase Database

        // SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchDocuments(query.trim().toUpperCase());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: live search
                return false;
            }
        });
    }

    private void fetchDocuments(String courseCode) {
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        // Query Firebase Database to get documents matching courseCode and category
        documentsRef.orderByChild("courseCode")
                .equalTo(courseCode)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    courseList.clear();
                    for (DataSnapshot docSnapshot : dataSnapshot.getChildren()) {
                        // Fetch metadata from Firebase Database
                        SourceDocumentModelClass item = docSnapshot.getValue(SourceDocumentModelClass.class);

                        // Retrieve the file name (metadata) from the model
                        if (item != null && item.getCr_pdfName() != null) {
                            // Now get the file URL from Firebase Storage
                            String fileName = item.getCr_pdfName();
                            StorageReference fileRef = storage.getReference().child("pdfs/" + fileName);

                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Update the model with the file URL
                                item.setCr_pdfUrl(uri.toString());
                                courseList.add(item);
                                adapter.notifyDataSetChanged();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
