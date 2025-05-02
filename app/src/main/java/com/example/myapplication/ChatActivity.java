package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class ChatActivity extends AppCompatActivity {

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
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // View bindings
        fileNameText = findViewById(R.id.file_name_text);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        inputComment = findViewById(R.id.input_comment);
        sendButton = findViewById(R.id.send_button);

        // Recycler setup
        commentList = new ArrayList<>();
        chatAdapter = new ChatAdapter(commentList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Chat messages reference
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

        // Load comments in real-time
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

        // Send button logic
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
