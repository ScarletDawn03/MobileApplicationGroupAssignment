package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PrivacySettings extends AppCompatActivity {
    private RadioGroup rgPrivacy;
    private RadioButton rbPublic, rbPeers, rbPrivate;
    private Button btnSave;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        // Bind views
        rgPrivacy = findViewById(R.id.rgPrivacy);
        rbPublic  = findViewById(R.id.rbPublic);
        //rbPeers   = findViewById(R.id.rbPeers);
        rbPrivate = findViewById(R.id.rbPrivate);
        btnSave   = findViewById(R.id.btnSavePrivacy);

        // Get current user UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();

        }else {
            userId = "U001"; // Test ID fallback
            Toast.makeText(this, "User not signed in, using test ID", Toast.LENGTH_SHORT).show();
        }

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Load existing or initialize default
        userRef.child("privacyLevel")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String level = snapshot.getValue(String.class);
                        if (level == null) {
                            // Initialize default to public
                            userRef.child("privacyLevel").setValue("public");
                            rbPublic.setChecked(true);
                        } else {
                            switch (level) {
                              //  case "peers": rbPeers.setChecked(true); break;
                                case "private": rbPrivate.setChecked(true); break;
                                default: rbPublic.setChecked(true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(PrivacySettings.this,
                                "Failed to load privacy settings.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Save button
        btnSave.setOnClickListener(v -> {
            String selected;
            int id = rgPrivacy.getCheckedRadioButtonId();
            if (id == R.id.rbPublic) selected = "public";
            else selected ="private";
            //else (id == R.id.rbPrivate) selected = "private";
            //else selected = "public";

            userRef.child("privacyLevel").setValue(selected)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Privacy saved", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
