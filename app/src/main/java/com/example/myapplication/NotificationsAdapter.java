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

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
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
        editor.putString("update_list", updatedUpdates);
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
