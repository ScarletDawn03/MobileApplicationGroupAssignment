package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

//This is the real feature for notification preferences
public class NotificationPreferences extends AppCompatActivity {
    private SwitchCompat switchLikesComments, switchNewUploads, switchMessages;
    private Button btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notfication_preferences);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        switchLikesComments = findViewById(R.id.switchLikesComments);
        switchNewUploads   = findViewById(R.id.switchNewUploads);
        switchMessages     = findViewById(R.id.switchMessages);
        btnSave            = findViewById(R.id.btnSaveNotifications);

        // Load saved preferences (default true)
        switchLikesComments.setChecked(
                prefs.getBoolean("pref_notify_likes_comments", true));
        switchNewUploads.setChecked(
                prefs.getBoolean("pref_notify_new_uploads", true));
        switchMessages.setChecked(
                prefs.getBoolean("pref_notify_messages", true));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_notify_likes_comments",
                    switchLikesComments.isChecked());
            editor.putBoolean("pref_notify_new_uploads",
                    switchNewUploads.isChecked());
            editor.putBoolean("pref_notify_messages",
                    switchMessages.isChecked());
            editor.apply();

            Toast.makeText(this,
                    "Notification preferences saved",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
