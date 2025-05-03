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

    // Each Medal benchmark
    private static final int BRONZE_MAX = AchievementConstant.SILVER_MIN - 1;
    private static final int SILVER_MAX = AchievementConstant.GOLD_MIN - 1;
    private static final int GOLD_MAX   = AchievementConstant.MAX_UPLOADS - 1;

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

        //To retrieve current user information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        String userEmail = user.getEmail();  // Get current user's email
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        /** A function to check the current user total upload amount and latest uploaded date**/
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


            //Display error message to user if it is failed
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AchievementActivity.this, "Failed to load uploads", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Convert the date into readable format
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

    //Function of dynamic update User Interface based on amount uploaded
    private void updateUI(int count, String latestDate) {
        tvCount.setText("Total Uploads: " + count);
        progressBar.setMax(GOLD_MAX);
        progressBar.setProgress(count);

        if (count <= BRONZE_MAX) {
            imgMedal.setImageResource(R.drawable.medal_bronze);
            tvNextTier.setText("Next: Silver at " + AchievementConstant.SILVER_MIN + " uploads");
        } else if (count <= SILVER_MAX) {
            imgMedal.setImageResource(R.drawable.medal_silver);
            tvNextTier.setText("Next: Gold at " + AchievementConstant.GOLD_MIN + " uploads");
        } else {
            imgMedal.setImageResource(R.drawable.medal_gold);
            tvNextTier.setText("Max tier achieved!");
        }

        if (latestDate != null) {
            tvLatestUploadDate.setText("Latest Upload: " + formatDate(latestDate));
        } else {
            tvLatestUploadDate.setText("Latest Upload: " + formatDate(latestDate));

        }
        // Function of add progress percentage
        double progressPercent = (double) count / GOLD_MAX * 100;
        String progressText = String.format(Locale.getDefault(),
                "Progress: %.1f%% to Gold", progressPercent);
    }
}
