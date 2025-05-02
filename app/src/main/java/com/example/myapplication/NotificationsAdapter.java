package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RecyclerView Adapter to display a list of notification strings and allow users to remove them.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<String> updates; // List of update messages (notifications)
    private Context context;      // Context to access resources and SharedPreferences

    public NotificationsAdapter(List<String> updates, Context context) {
        this.updates = updates;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for individual notification items
        View view = LayoutInflater.from(context).inflate(R.layout.item_update, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String update = updates.get(position);
        holder.notificationText.setText(update); // Set notification text

        ImageView fileTypeIcon = holder.fileTypeIcon;

        // Choose the icon based on file type
        if (update.endsWith(".pdf")) {
            fileTypeIcon.setImageResource(R.drawable.pdf);
        } else if (update.endsWith(".doc")) {
            fileTypeIcon.setImageResource(R.drawable.doc);
        } else if (update.endsWith(".docx")) {
            fileTypeIcon.setImageResource(R.drawable.docx);
        } else {
            fileTypeIcon.setImageResource(R.drawable.pdf); // default icon
        }

        // Handle the click event on the "remove" icon
        holder.removeUpdateButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                String itemToRemove = updates.get(pos);

                // Remove the notification from SharedPreferences
                removeUpdateFromPreferences(itemToRemove);

                // Remove from in-memory list and update the UI
                updates.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return updates.size(); // Total number of notifications
    }

    /**
     * Removes a specific update string from SharedPreferences storage.
     */
    private void removeUpdateFromPreferences(String updateToRemove) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String existingUpdates = sharedPreferences.getString("update_list", "");

        // Convert stored string to list, remove item, then re-save as a joined string
        List<String> updatesList = new ArrayList<>(Arrays.asList(existingUpdates.split("\n")));
        updatesList.remove(updateToRemove);

        String updatedUpdates = String.join("\n", updatesList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update_list", updatedUpdates);
        editor.apply();
    }

    /**
     * Replaces the current data with a new list and refreshes the RecyclerView.
     */
    public void updateData(List<String> updatedList) {
        updates.clear();
        updates.addAll(updatedList);
        notifyDataSetChanged();  // Refresh UI with new data
    }

    /**
     * ViewHolder class to hold references to the views for each notification item.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationText;
        ImageView removeUpdateButton; // "X" button for deletion
        ImageView fileTypeIcon;       // Icon representing the file type

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationText = itemView.findViewById(R.id.updateTextView);
            removeUpdateButton = itemView.findViewById(R.id.removeImageView);
            fileTypeIcon = itemView.findViewById(R.id.fileTypeIcon);
        }
    }
}
