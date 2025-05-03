package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.List;
import java.util.Map;

/**
 * CourseAdapter binds a list of course PDF files to the RecyclerView.
 * It supports displaying course information, opening the PDF, liking, and commenting.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<SourceDocumentModelClass> courseList; // List of course documents
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String userEmail;
    private Context context;

    private TextView likesCount;

    /**
     * Constructor for CourseAdapter
     */
    public CourseAdapter(Context context, List<SourceDocumentModelClass> courseList, DatabaseReference databaseReference, String userEmail) {
        this.context = context;
        this.courseList = courseList;
        this.databaseReference = databaseReference;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        if (courseList.isEmpty()) return;

        SourceDocumentModelClass course = courseList.get(position);
        String pdfUrl = course.getCr_pdfUrl(); // URL to the PDF file
        holder.bind(course, pdfUrl); // Bind course data to the views

        // Get or initialize likedBy map to track who liked the document
        Map<String, Boolean> initialLikedByMap = course.getLiked_by();
        final Map<String, Boolean> likedByMap = (initialLikedByMap != null) ? initialLikedByMap : new HashMap<>();

        // Get a Firebase-safe version of the current user's email
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String safeEmail = userEmail.replace(".", "_");

        // Set the correct icon based on whether the user has liked the document
        if (likedByMap.containsKey(safeEmail) && likedByMap.get(safeEmail)) {
            holder.likeButton.setImageResource(R.drawable.like);
        } else {
            holder.likeButton.setImageResource(R.drawable.unlike);
        }


        DatabaseReference likesRef = databaseReference.child(course.getKey()).child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer likeCount = snapshot.getValue(Integer.class);
                if (likeCount != null) {
                    holder.likesCount.setText("Number of likes: " + likeCount);
                } else {
                    holder.likesCount.setText("Number of likes: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.likesCount.setText("Number of likes: 0");
            }
        });

        // Like button click logic
        holder.likeButton.setOnClickListener(v -> {
            Map<String, Boolean> updatedLikedByMap = new HashMap<>(likedByMap);

            if (updatedLikedByMap.containsKey(safeEmail)) {
                updatedLikedByMap.remove(safeEmail); // Unlike
                holder.likeButton.setImageResource(R.drawable.unlike);
                Toast.makeText(v.getContext(), "Unliked", Toast.LENGTH_SHORT).show();
            } else {
                updatedLikedByMap.put(safeEmail, true); // Like
                holder.likeButton.setImageResource(R.drawable.like);
                Toast.makeText(v.getContext(), "Liked", Toast.LENGTH_SHORT).show();
            }

            // Update Firebase with the new like state
            updateLikeInDatabase(course.getKey(), updatedLikedByMap, v.getContext());
        });

        // Comment button click: Launch chat activity for the current document
        holder.commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("file_key", course.getKey()); // Pass document ID to chat
            context.startActivity(intent);
        });

        // Set PDF name or fallback
        String pdfName = course.getCr_pdfName();
        if (pdfName != null && !pdfName.isEmpty()) {
            holder.pdfName.setText(pdfName);
        } else {
            holder.pdfName.setText("No PDF Name");
        }

        // Set created_at or fallback
        String createdAt = course.getCreated_at();
        if (createdAt != null && !createdAt.isEmpty()) {
            holder.createdAt.setText("Created At: " + createdAt);
        } else {
            holder.createdAt.setText("Creation date not available");
        }

        // Set created_by or fallback
        String createdBy = course.getCreated_by();
        if (createdBy != null && !createdBy.isEmpty()) {
            holder.createdBy.setText("Created By: " + createdBy);
        } else {
            holder.createdBy.setText("Creator not available");
        }

        // When item is clicked, open the PDF URL in browser
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
        return courseList.size(); // Total items in the list
    }

    /**
     * Update the liked_by and like count in the Firebase database
     */
    private void updateLikeInDatabase(String documentId, Map<String, Boolean> likedByMap, Context context) {
        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        }

        DatabaseReference courseRef = databaseReference.child(documentId);
        DatabaseReference likesRef = courseRef.child("likes");
        DatabaseReference likedByRef = courseRef.child("liked_by");

        // First update liked_by map
        likedByRef.setValue(likedByMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Then update the like count based on the map size
                        likesRef.get().addOnCompleteListener(likeTask -> {
                            if (likeTask.isSuccessful()) {
                                Integer currentLikes = likeTask.getResult().getValue(Integer.class);
                                if (currentLikes == null) currentLikes = 0;
                                int updatedLikes = likedByMap.size();
                                likesRef.setValue(updatedLikes);
                            }
                        });
                    } else {
                        Toast.makeText(context, "Error updating like status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ViewHolder class to represent each course item in the RecyclerView
     */
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView pdfName;
        private TextView createdAt;
        private TextView createdBy;
        private ImageView likeButton;
        private ImageButton commentButton;
        private TextView likesCount;

        public CourseViewHolder(View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);          // PDF name text view
            createdAt = itemView.findViewById(R.id.created_at);      // Created at text view
            createdBy = itemView.findViewById(R.id.created_by);      // Created by text view
            likesCount = itemView.findViewById(R.id.likes_count);
            likeButton = itemView.findViewById(R.id.like_button);    // Like button
            commentButton = itemView.findViewById(R.id.comment_button); // Comment button
        }

        public void bind(SourceDocumentModelClass course, String pdfUrl) {
            // Bind model data to the views
            pdfName.setText(course.getCr_pdfName());
            createdAt.setText(course.getCreated_at());
            createdBy.setText(course.getCreated_by());
            likesCount = itemView.findViewById(R.id.likes_count);
        }
    }
}
