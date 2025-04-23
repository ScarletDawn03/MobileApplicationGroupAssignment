package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;

public class CompleteProfileActivity extends AppCompatActivity {

    private EditText fullNameField, contactNumberField, dateOfBirthField, genderField;
    private Button saveProfileBtn, cancelBtn, uploadImageBtn;
    private ImageView profilePhotoView, selectedPhotoView;

    private DatabaseReference mDatabase;
    private String uid;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    private StorageReference storageReference;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("profile_photos");

        // Views
        fullNameField = findViewById(R.id.et_fullname);
        contactNumberField = findViewById(R.id.et_contactNumber);
        dateOfBirthField = findViewById(R.id.et_dateOfBirth);
        genderField = findViewById(R.id.et_gender);
        profilePhotoView = findViewById(R.id.iv_profile_photo);
        selectedPhotoView = findViewById(R.id.iv_selected_photo);
        saveProfileBtn = findViewById(R.id.btn_saveProfile);
        cancelBtn = findViewById(R.id.btn_cancel);
        uploadImageBtn = findViewById(R.id.btn_upload_image);

        // Google sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Date picker
        dateOfBirthField.setOnClickListener(v -> showDatePicker());

        // Upload image button
        uploadImageBtn.setOnClickListener(v -> chooseProfileImage());

        // Save button
        saveProfileBtn.setOnClickListener(v -> saveUserProfile());

        // Cancel button
        cancelBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(CompleteProfileActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthSelected, daySelected) -> {
                    String selectedDate = daySelected + "/" + (monthSelected + 1) + "/" + yearSelected;
                    dateOfBirthField.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void chooseProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        selectedPhotoView.setImageBitmap(bitmap);
                        selectedPhotoView.setVisibility(ImageView.VISIBLE);
                        profilePhotoView.setVisibility(ImageView.INVISIBLE); // Hide default image
                        uploadImageBtn.setText("Image Selected");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private void saveUserProfile() {
        String fullName = fullNameField.getText().toString().trim();
        String contactNumber = contactNumberField.getText().toString().trim();
        String dateOfBirth = dateOfBirthField.getText().toString().trim();
        String gender = genderField.getText().toString().trim();

        if (fullName.isEmpty() || contactNumber.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save data
        mDatabase.child("users").child(uid).child("fullName").setValue(fullName);
        mDatabase.child("users").child(uid).child("contactNumber").setValue(contactNumber);
        mDatabase.child("users").child(uid).child("dateOfBirth").setValue(dateOfBirth);
        mDatabase.child("users").child(uid).child("gender").setValue(gender);

        // Upload image if selected
        if (selectedImageUri != null) {
            String fileName = currentUser.getDisplayName() + ".jpg";
            StorageReference fileRef = storageReference.child(fileName);

            fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    mDatabase.child("users").child(uid).child("profilePhotoUrl").setValue(imageUrl)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(CompleteProfileActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to save profile image URL", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            startActivity(new Intent(CompleteProfileActivity.this, MainActivity.class));
            finish();
        }
    }
}
