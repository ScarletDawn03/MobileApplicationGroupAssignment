package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChatActivity handles the chat interface for a specific course file.
 * It allows users to view the PDF file name, read comments, and post new comments.
 *
 */
public class ChatActivity extends AppCompatActivity {

    //Initialization
    private RecyclerView chatRecyclerView;
    private EditText inputComment;
    private Button sendButton;
    private TextView fileNameText;

    private DatabaseReference chatRef;
    private String fileKey;
    private List<CommentModel> commentList;
    private ChatAdapter chatAdapter;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve file key from intent
        fileKey = getIntent().getStringExtra("file_key");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userEmail = auth.getCurrentUser().getEmail();
        } else {
            // Redirect to home page
            Toast.makeText(this, "You must be logged in to use the chat.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish(); // Close current activity
            return;
        }

        // Bind views from layout
        fileNameText = findViewById(R.id.file_name_text);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        inputComment = findViewById(R.id.input_comment);
        sendButton = findViewById(R.id.send_button);

        // Initialize RecyclerView and adapter
        commentList = new ArrayList<>();
        chatAdapter = new ChatAdapter(commentList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Reference to the chat node in Firebase
        chatRef = FirebaseDatabase.getInstance()
                .getReference("courses").child(fileKey).child("chat");

        // Load and display file name
        DatabaseReference fileRef = FirebaseDatabase.getInstance()
                .getReference("courses").child(fileKey);
        fileRef.child("cr_pdfName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fileName = snapshot.getValue(String.class);
                if (fileName != null) {
                    fileNameText.setText(fileName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Real-time listener for comments
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CommentModel comment = snap.getValue(CommentModel.class);
                    commentList.add(comment);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Send button logic to submit new comment
        sendButton.setOnClickListener(v -> {
            String commentText = inputComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("comment", commentText);
                data.put("user", userEmail);
                data.put("timestamp", ServerValue.TIMESTAMP);

                chatRef.push().setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        inputComment.setText("");
                    }
                });
            }
        });
    }
}
