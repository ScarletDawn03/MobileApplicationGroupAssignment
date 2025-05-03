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
    private SwitchCompat switchLikesComments, switchNewUploads, switchDialog, switchNotification;
    private Button btnSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notfication_preferences);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        switchLikesComments = findViewById(R.id.switchLikesComments);
        switchNewUploads   = findViewById(R.id.switchNewUploads);
        switchDialog = findViewById(R.id.switchDialog);
        switchNotification = findViewById(R.id.switchNotifications);
        btnSave            = findViewById(R.id.btnSaveNotifications);

        // Load saved preferences (default true)
        switchLikesComments.setChecked(
                prefs.getBoolean("pref_notify_likes_comments", true));
        switchNewUploads.setChecked(
                prefs.getBoolean("pref_notify_new_uploads", true));
        switchDialog.setChecked(
                prefs.getBoolean("pref_show_dialogs", true));
        switchNotification.setChecked(
                prefs.getBoolean("pref_show_notifications", true));

        //save the latest of user preferences based on the toggle button
        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_notify_likes_comments",
                    switchLikesComments.isChecked());
            editor.putBoolean("pref_notify_new_uploads",
                    switchNewUploads.isChecked());
            editor.putBoolean("pref_show_dialogs", switchDialog.isChecked());
            editor.putBoolean("pref_show_notifications", switchNotification.isChecked());
            editor.apply();

            //Display message to show succeed
            Toast.makeText(this,
                    "Notification preferences saved",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
