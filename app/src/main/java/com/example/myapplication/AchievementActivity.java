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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AchievementActivity extends AppCompatActivity {
    private ImageView imgMedal;
    private TextView tvCount, tvNextTier;
    private ProgressBar progressBar;
    private Button btnBack;
    private TextView tvLatestUploadDate;

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
        tvLatestUploadDate = findViewById(R.id.tvLatestUploadDate);
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



        String userEmail = user.getEmail();  // Get current user's email
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");


        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int count = 0;
                String latestDate = null;

                for (DataSnapshot child : snapshot.getChildren()) {
                    UploadItem item = child.getValue(UploadItem.class);
                    // only count this upload if it was done by the current user:
                    if (item != null && userEmail.equals(item.getCreated_by())) {
                        count++;

                        String createdAt = item.getCreated_at();
                        if (createdAt != null && (latestDate == null || createdAt.compareTo(latestDate) > 0)) {
                            latestDate = createdAt;
                        }
                    }
                }

                updateUI(count, latestDate);
            }


            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AchievementActivity.this, "Failed to load uploads", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String formatDate(String isoDateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(isoDateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDateString; // fallback
        }
    }

    private void updateUI(int count, String latestDate) {
        tvCount.setText("Total Uploads: " + count);
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

        if (latestDate != null) {
            tvLatestUploadDate.setText("Latest Upload: " + formatDate(latestDate));
        } else {
            tvLatestUploadDate.setText("Latest Upload: " + formatDate(latestDate));
        }
    }
}
