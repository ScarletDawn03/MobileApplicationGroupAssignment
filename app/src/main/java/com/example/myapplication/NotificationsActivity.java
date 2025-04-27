package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter adapter;
    private List<String> updatesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);

        // Load updates from SharedPreferences
        loadUpdatesFromPreferences();

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(updatesList, this);
        notificationsRecyclerView.setAdapter(adapter);
    }

    // Method to load updates from SharedPreferences
    private void loadUpdatesFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String existingUpdates = sharedPreferences.getString("update_list", "");

        // Clear the list to ensure we're reloading it fresh
        updatesList.clear();

        if (!existingUpdates.isEmpty()) {
            String[] updatesArray = existingUpdates.split("\n");
            updatesList.addAll(Arrays.asList(updatesArray));
        }
    }

    // NotificationsAdapter class for managing RecyclerView
    public static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
        private List<String> updates;
        private Context context;

        public NotificationsAdapter(List<String> updates, Context context) {
            this.updates = updates;
            this.context = context;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_update, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            String update = updates.get(position);
            holder.notificationText.setText(update);

            // Set an OnClickListener to the X button to remove the update
            holder.removeUpdateButton.setOnClickListener(v -> {
                // Call removeUpdateFromPreferences when X button is clicked
                removeUpdateFromPreferences(update);

                // Remove the update from the list and notify the adapter
                updates.remove(position);
                notifyItemRemoved(position);
            });
        }

        @Override
        public int getItemCount() {
            return updates.size();
        }

        // Method to remove an update from SharedPreferences
        private void removeUpdateFromPreferences(String updateToRemove) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String existingUpdates = sharedPreferences.getString("update_list", "");

            // Remove the selected update from the string
            String updatedUpdates = existingUpdates.replace("\n" + updateToRemove, "");

            // Save the updated list back to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("update_list", updatedUpdates.trim());  // Make sure there's no trailing newline
            editor.apply();
        }

        public static class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView notificationText;
            ImageView removeUpdateButton; // X button

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                notificationText = itemView.findViewById(R.id.updateTextView);
                removeUpdateButton = itemView.findViewById(R.id.removeImageView); // X button
            }
        }
    }
}
