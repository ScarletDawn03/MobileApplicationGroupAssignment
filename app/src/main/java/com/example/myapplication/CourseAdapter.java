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
import android.content.Context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        if (courseList.isEmpty()) return;

        SourceDocumentModelClass course = courseList.get(position);
        String pdfUrl = course.getCr_pdfUrl();
        holder.bind(course, pdfUrl);

        // Safely initialize likedByMap
        Map<String, Boolean> initialLikedByMap = course.getLiked_by();
        final Map<String, Boolean> likedByMap = (initialLikedByMap != null) ? initialLikedByMap : new HashMap<>();

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String safeEmail = userEmail.replace(".", "_");

        if (likedByMap.containsKey(safeEmail) && likedByMap.get(safeEmail)) {
            holder.likeButton.setImageResource(R.drawable.like);
        } else {
            holder.likeButton.setImageResource(R.drawable.unlike);
        }

        holder.likeButton.setOnClickListener(v -> {
            Map<String, Boolean> updatedLikedByMap = new HashMap<>(likedByMap); // Safe now

            if (updatedLikedByMap.containsKey(safeEmail)) {
                updatedLikedByMap.remove(safeEmail);
                holder.likeButton.setImageResource(R.drawable.unlike);
                Toast.makeText(v.getContext(), "Unliked", Toast.LENGTH_SHORT).show();
            } else {
                updatedLikedByMap.put(safeEmail, true);
                holder.likeButton.setImageResource(R.drawable.like);
                Toast.makeText(v.getContext(), "Liked", Toast.LENGTH_SHORT).show();
            }

            updateLikeInDatabase(course.getKey(), updatedLikedByMap, v.getContext());
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

    private void updateLikeInDatabase(String documentId, Map<String, Boolean> likedByMap, Context context) {
        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        }

        // Reference to the course document by its unique documentId
        DatabaseReference courseRef = databaseReference.child(documentId);
        DatabaseReference likesRef = courseRef.child("likes");
        DatabaseReference likedByRef = courseRef.child("liked_by");

        // Update the liked_by map with the current user's like status
        likedByRef.setValue(likedByMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the current number of likes and update
                        likesRef.get().addOnCompleteListener(likeTask -> {
                            if (likeTask.isSuccessful()) {
                                Integer currentLikes = likeTask.getResult().getValue(Integer.class);
                                if (currentLikes == null) currentLikes = 0;
                                // Update like count based on the size of the liked_by map
                                int updatedLikes = likedByMap.size();
                                likesRef.setValue(updatedLikes);
                            }
                        });
                    } else {
                        Toast.makeText(context, "Error updating like status", Toast.LENGTH_SHORT).show();
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
