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
import java.util.Arrays;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";
    private NotificationsAdapter adapter;
    private final List<String> notificationsList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        adapter = new NotificationsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Load initial data
        loadNotifications();
    }

    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedNotifications = prefs.getString("update_list", "").trim(); // Trim whitespace

        notificationsList.clear();

        if (!savedNotifications.isEmpty()) {
            // Split and filter out empty strings
            String[] notificationsArray = savedNotifications.split("\n");
            for (String notification : notificationsArray) {
                if (!notification.trim().isEmpty()) {  // Only add non-empty strings
                    notificationsList.add(notification.trim());
                }
            }
            Log.d(TAG, "Loaded " + notificationsList.size() + " notifications");
        } else {
            Log.d(TAG, "No notifications found in preferences");
        }

        updateAdapterData();
    }

    private void updateAdapterData() {
        if (adapter != null) {
            adapter.updateData(notificationsList);
        } else {
            Log.e(TAG, "Adapter is null when trying to update data");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications(); // Refresh data when returning to activity
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method to show toast messages for debugging
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}