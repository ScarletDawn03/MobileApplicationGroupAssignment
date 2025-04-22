package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CompleteProfileActivity extends AppCompatActivity {

    private EditText fullNameField, contactNumberField, dateOfBirthField, genderField;
    private Button saveProfileBtn, cancelBtn;
    private DatabaseReference mDatabase;
    private String uid;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        // Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get UID of current user
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        fullNameField = findViewById(R.id.et_fullname);
        contactNumberField = findViewById(R.id.et_contactNumber);
        dateOfBirthField = findViewById(R.id.et_dateOfBirth);
        genderField = findViewById(R.id.et_gender);
        saveProfileBtn = findViewById(R.id.btn_saveProfile);
        cancelBtn = findViewById(R.id.btn_cancel);

        // Setup Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Date picker for dateOfBirth
        dateOfBirthField.setOnClickListener(v -> showDatePicker());

        // Save button logic
        saveProfileBtn.setOnClickListener(v -> {
            String fullName = fullNameField.getText().toString().trim();
            String contactNumber = contactNumberField.getText().toString().trim();
            String dateOfBirth = dateOfBirthField.getText().toString().trim();
            String gender = genderField.getText().toString().trim();

            if (fullName.isEmpty() || contactNumber.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty()) {
                Toast.makeText(CompleteProfileActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            mDatabase.child("users").child(uid).child("fullName").setValue(fullName);
            mDatabase.child("users").child(uid).child("contactNumber").setValue(contactNumber);
            mDatabase.child("users").child(uid).child("dateOfBirth").setValue(dateOfBirth);
            mDatabase.child("users").child(uid).child("gender").setValue(gender)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(CompleteProfileActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(CompleteProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Cancel button logic
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
                CompleteProfileActivity.this,
                (view, yearSelected, monthSelected, daySelected) -> {
                    String selectedDate = daySelected + "/" + (monthSelected + 1) + "/" + yearSelected;
                    dateOfBirthField.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}
