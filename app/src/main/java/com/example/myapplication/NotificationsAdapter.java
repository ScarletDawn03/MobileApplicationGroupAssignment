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

        holder.removeUpdateButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                String itemToRemove = updates.get(pos);
                removeUpdateFromPreferences(itemToRemove);
                updates.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }


    @Override
    public int getItemCount() {
        return updates.size();
    }

    private void removeUpdateFromPreferences(String updateToRemove) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String existingUpdates = sharedPreferences.getString("update_list", "");

        List<String> updatesList = new ArrayList<>(Arrays.asList(existingUpdates.split("\n")));
        updatesList.remove(updateToRemove);

        String updatedUpdates = String.join("\n", updatesList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update_list", updatedUpdates);
        editor.apply();
    }

    public void updateData(List<String> updatedList) {
        updates.clear();
        updates.addAll(updatedList);
        notifyDataSetChanged();  // This will refresh the RecyclerView with the updated list
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
