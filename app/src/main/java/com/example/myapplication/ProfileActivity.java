package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private ImageView imgProfile, btnEditPhoto;
    private ImageView iconEditFullName, iconEditUsername, iconEditContact;
    private EditText editFullName, editUsername, editContact;
    private TextView txtGender, txtDOB, txtEmail;
    private Button btnUpdateInfo;

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind views
        imgProfile = findViewById(R.id.imgProfile);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);

        editFullName = findViewById(R.id.editFullName);
        editUsername = findViewById(R.id.editUsername);
        editContact = findViewById(R.id.editContact);

        txtGender = findViewById(R.id.txtGender);
        txtDOB = findViewById(R.id.txtDOB);
        txtEmail = findViewById(R.id.txtEmail);

        iconEditFullName = findViewById(R.id.iconEditFullName);
        iconEditUsername = findViewById(R.id.iconEditUsername);
        iconEditContact = findViewById(R.id.iconEditContact);
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo);

        // Determine user ID dynamically
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "U001"; // Test ID fallback
            Toast.makeText(this, "User not signed in, using test ID", Toast.LENGTH_SHORT).show();
        }

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_photos");

        // Load profile photo and data
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String photoUrl = snapshot.child("profilePhotoUrl").getValue(String.class);
                if (photoUrl != null) Glide.with(this).load(photoUrl).into(imgProfile);

                editFullName.setText(snapshot.child("fullName").getValue(String.class));
                editUsername.setText(snapshot.child("username").getValue(String.class));
                editContact.setText(snapshot.child("contactNumber").getValue(String.class));

                txtGender.setText(snapshot.child("gender").getValue(String.class));
                txtDOB.setText(snapshot.child("dateOfBirth").getValue(String.class));
                txtEmail.setText(snapshot.child("email").getValue(String.class));
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

        // Disable editing by default
        editFullName.setEnabled(false);
        editUsername.setEnabled(false);
        editContact.setEnabled(false);

        // Enable editing when tapping pencil icons
        iconEditFullName.setOnClickListener(v -> editFullName.setEnabled(true));
        iconEditUsername.setOnClickListener(v -> editUsername.setEnabled(true));
        iconEditContact.setOnClickListener(v -> editContact.setEnabled(true));

        // Photo selection
        btnEditPhoto.setOnClickListener(v -> openFileChooser());

        // Save changes
        btnUpdateInfo.setOnClickListener(v -> {
            String newFullName = editFullName.getText().toString().trim();
            String newUsername = editUsername.getText().toString().trim();
            String newContact = editContact.getText().toString().trim();

            dbRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String currentFullName = snapshot.child("fullName").getValue(String.class);
                    String currentUsername = snapshot.child("username").getValue(String.class);
                    String currentContact = snapshot.child("contactNumber").getValue(String.class);

                    boolean isFullNameChanged = !newFullName.equals(currentFullName);
                    boolean isUsernameChanged = !newUsername.equals(currentUsername);
                    boolean isContactChanged = !newContact.equals(currentContact);

                    if (!isFullNameChanged && !isUsernameChanged && !isContactChanged && imageUri == null) {
                        Toast.makeText(this, "No changes to update.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseDatabase.getInstance().getReference("users").get().addOnSuccessListener(allSnapshot -> {
                        StringBuilder errorMessage = new StringBuilder();

                        for (DataSnapshot userSnapshot : allSnapshot.getChildren()) {
                            if (userSnapshot.getKey().equals(userId)) continue; // Skip current user

                            String otherFullName = userSnapshot.child("fullName").getValue(String.class);
                            String otherUsername = userSnapshot.child("username").getValue(String.class);
                            String otherContact = userSnapshot.child("contactNumber").getValue(String.class);

                            if (isFullNameChanged && newFullName.equals(otherFullName)) {
                                errorMessage.append("Full name already exists.\n");
                            }
                            if (isUsernameChanged && newUsername.equals(otherUsername)) {
                                errorMessage.append("Username already exists.\n");
                            }
                            if (isContactChanged && newContact.equals(otherContact)) {
                                errorMessage.append("Contact number already exists.\n");
                            }
                        }

                        if (errorMessage.length() > 0) {
                            Toast.makeText(this, errorMessage.toString().trim(), Toast.LENGTH_LONG).show();
                        } else {
                            Map<String, Object> updates = new HashMap<>();
                            if (isFullNameChanged) updates.put("fullName", newFullName);
                            if (isUsernameChanged) updates.put("username", newUsername);
                            if (isContactChanged) updates.put("contactNumber", newContact);

                            dbRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                editFullName.setEnabled(false);
                                editUsername.setEnabled(false);
                                editContact.setEnabled(false);
                            }).addOnFailureListener(e ->
                                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                            if (imageUri != null) uploadImageToFirebase();
                        }
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Error checking duplicates: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to load profile for update: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }

    //Enable the user to open gallery from phone
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //Function of setting the image on the profile
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    //Function to upload the latest image to firebase storage
    private void uploadImageToFirebase() {
        StorageReference fileRef = storageRef.child(userId + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(task ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    dbRef.child("profilePhotoUrl").setValue(downloadUrl);
                    Glide.with(this).load(downloadUrl).into(imgProfile);
                    Toast.makeText(this, "Profile photo uploaded!", Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
