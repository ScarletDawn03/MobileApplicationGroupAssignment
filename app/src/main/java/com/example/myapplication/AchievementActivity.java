package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class AchievementActivity extends AppCompatActivity {
    private ImageView imgMedal;
    private TextView tvCount, tvNextTier;
    private ProgressBar progressBar;
    private Button btnBack;

    // Medal thresholds
    private static final int BRONZE_MAX = 33;
    private static final int SILVER_MAX = 66;
    private static final int GOLD_MAX   = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acheivement);

        imgMedal    = findViewById(R.id.imgMedal);
        tvCount     = findViewById(R.id.tvCount);
        tvNextTier  = findViewById(R.id.tvNextTier);
        progressBar = findViewById(R.id.progressBar);
        btnBack     = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        //Try to retreive current user information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Getting the userID from upload file (If created) (Need to be modified)
        DatabaseReference uploadsRef = FirebaseDatabase.getInstance()
                .getReference("uploads")//Based on upload file in firebase (need to modified)
                .child(user.getUid());

        uploadsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                updateUI(count);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AchievementActivity.this,
                        "Failed to load uploads", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(int count) {
        tvCount.setText("Uploads: " + count);
        progressBar.setMax(GOLD_MAX);
        progressBar.setProgress(count);

        if (count <= BRONZE_MAX) {
            imgMedal.setImageResource(R.drawable.medal_bronze);
            tvNextTier.setText("Next: Silver at " + (BRONZE_MAX+1) + " uploads");
        } else if (count <= SILVER_MAX) {
            imgMedal.setImageResource(R.drawable.medal_silver);
            tvNextTier.setText("Next: Gold at " + (SILVER_MAX+1) + " uploads");
        } else {
            imgMedal.setImageResource(R.drawable.medal_gold);
            tvNextTier.setText("Max tier achieved!");
        }
    }
}
