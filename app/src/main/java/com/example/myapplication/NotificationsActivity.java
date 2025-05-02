package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a list of notification messages stored locally in SharedPreferences.
 */
public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";  // Tag for logging
    private NotificationsAdapter adapter;                        // Adapter to bind notifications to RecyclerView
    private final List<String> notificationsList = new ArrayList<>(); // Local list to hold notifications
    private RecyclerView recyclerView;                           // RecyclerView to display notifications

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Set up toolbar and enable back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Show back button
        }

        // Set up RecyclerView with a linear layout
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with an empty list and attach it to the RecyclerView
        adapter = new NotificationsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Load notifications from SharedPreferences
        loadNotifications();
    }

    /**
     * Loads notifications from SharedPreferences, splits them, filters out empty entries,
     * and updates the adapter's data.
     */
    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedNotifications = prefs.getString("update_list", "").trim();  // Get stored notifications

        notificationsList.clear();  // Clear old data

        if (!savedNotifications.isEmpty()) {
            // Split notifications by newline and add only non-empty ones
            String[] notificationsArray = savedNotifications.split("\n");
            for (String notification : notificationsArray) {
                if (!notification.trim().isEmpty()) {
                    notificationsList.add(notification.trim());
                }
            }
            Log.d(TAG, "Loaded " + notificationsList.size() + " notifications");
        } else {
            Log.d(TAG, "No notifications found in preferences");
        }

        updateAdapterData();
    }

    /**
     * Refreshes the RecyclerView adapter with new data.
     */
    private void updateAdapterData() {
        if (adapter != null) {
            adapter.updateData(notificationsList);  // Update adapter's dataset
        } else {
            Log.e(TAG, "Adapter is null when trying to update data");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();  // Refresh list when activity comes into view
    }

    /**
     * Handles toolbar back button click.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Deprecated but still used for now
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays a toast message (used for debugging).
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
