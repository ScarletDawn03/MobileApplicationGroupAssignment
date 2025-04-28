package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<SourceDocumentModelClass> courseList;
    private Set<String> likedCourses; // A set to store the liked courses' URLs
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private String userEmail;

    public CourseAdapter(List<SourceDocumentModelClass> courseList, DatabaseReference databaseReference, String userEmail) {
        this.courseList = courseList;
        this.databaseReference = databaseReference;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        if (courseList.isEmpty()) {
            // Handle empty list (you could show a placeholder if desired)
            return;
        }

        if (likedCourses == null) {
            likedCourses = new HashSet<>();
        }

        SourceDocumentModelClass course = courseList.get(position);

        String pdfUrl = course.getCr_pdfUrl();  // Assume you have a method to get the URL
        holder.bind(course, pdfUrl);


        // Check if the course is already liked
        if (likedCourses.contains(pdfUrl)) {
            holder.likeButton.setImageResource(R.drawable.like);  // Change to "liked"
        } else {
            holder.likeButton.setImageResource(R.drawable.unlike);  // Change to "unliked"
        }

        // Handle the like button click event
        holder.likeButton.setOnClickListener(v -> {
            if (likedCourses.contains(pdfUrl)) {
                likedCourses.remove(pdfUrl);  // Remove from liked list
                holder.likeButton.setImageResource(R.drawable.unlike);  // Change icon to "unlike"
                Toast.makeText(v.getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                updateLikeInDatabase(pdfUrl, false);  // Update in Firebase, "unlike"
            } else {
                likedCourses.add(pdfUrl);  // Add to liked list
                holder.likeButton.setImageResource(R.drawable.like);  // Change icon to "liked"
                Toast.makeText(v.getContext(), "Liked", Toast.LENGTH_SHORT).show();
                updateLikeInDatabase(pdfUrl, true);  // Update in Firebase, "like"
            }
        });


        // Set the PDF name
        String pdfName = course.getCr_pdfName();  // Assuming the field is cr_pdfName
        if (pdfName != null && !pdfName.isEmpty()) {
            holder.pdfName.setText(pdfName);  // Set the PDF name in the TextView
        } else {
            holder.pdfName.setText("No PDF Name");  // Set default text if PDF name is missing
        }

        // Set the created_at field
        String createdAt = course.getCreated_at();  // Assuming the field is created_at
        if (createdAt != null && !createdAt.isEmpty()) {
            holder.createdAt.setText("Created At: " + createdAt);  // Display "Created At"
        } else {
            holder.createdAt.setText("Creation date not available");
        }

        // Set the created_by field
        String createdBy = course.getCreated_by();  // Assuming the field is created_by
        if (createdBy != null && !createdBy.isEmpty()) {
            holder.createdBy.setText("Created By: " + createdBy);  // Display "Created By"
        } else {
            holder.createdBy.setText("Creator not available");
        }

        // Set up a listener to open the PDF URL when clicked
        holder.itemView.setOnClickListener(v -> {
            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                v.getContext().startActivity(browserIntent);
            } else {
                Toast.makeText(v.getContext(), "No PDF available", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return courseList.size();
    }


    // Method to update like status in Firebase
    private void updateLikeInDatabase(String pdfUrl, boolean isLiked) {

        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // assuming user is logged in
        String safeEmail = userEmail.replace(".", "_"); // Safe format for email, to be used as the key in liked_by


        // Find the course in Firebase by pdfUrl or documentId
        databaseReference.orderByChild("cr_pdfUrl").equalTo(pdfUrl)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String courseId = snapshot.getKey();  // Get the course's unique ID

                            Log.d("Course ID", "Course ID: " + courseId);  // Log the course ID for debugging
                            DatabaseReference courseRef = databaseReference.child(courseId);
                            DatabaseReference likesRef = courseRef.child("likes");
                            DatabaseReference likedByRef=courseRef.child("liked_by");

                            if (isLiked) {

                                likedByRef.child(safeEmail).setValue(true);

                                // Increment like count
                                likesRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Integer currentLikes = task.getResult().getValue(Integer.class);
                                        if (currentLikes == null) currentLikes = 0;
                                        likesRef.setValue(currentLikes + 1);
                                    }
                                });
                            } else {
                                // Decrement the like count
                                likedByRef.child(safeEmail).removeValue();

                                // Decrement like count
                                likesRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Integer currentLikes = task.getResult().getValue(Integer.class);
                                        if (currentLikes != null && currentLikes > 0) {
                                            likesRef.setValue(currentLikes - 1);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }


    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView pdfName;
        private TextView createdAt;

        private TextView createdBy;
        private ImageView likeButton;

        public CourseViewHolder(View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);  // TextView for course title
            createdAt = itemView.findViewById(R.id.created_at);  // TextView for course description
            createdBy = itemView.findViewById(R.id.created_by);  // TextView for course duration
            likeButton = itemView.findViewById(R.id.like_button);  // ImageView for like button
        }

        public void bind(SourceDocumentModelClass course, String pdfUrl) {
            // Bind course data to the views
            pdfName.setText(course.getCr_pdfName());  // Course title
            createdAt.setText(course.getCreated_at());  // Course description
            createdBy.setText(course.getCreated_by());  // Course duration
        }
    }


}
