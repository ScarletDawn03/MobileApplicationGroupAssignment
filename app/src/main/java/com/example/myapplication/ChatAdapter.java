package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<CommentModel> commentList;

    private long timestamp;


    public ChatAdapter(List<CommentModel> commentList) {
        this.commentList = commentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, userText, timestampText;
        public ViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            userText = itemView.findViewById(R.id.user_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        holder.commentText.setText(comment.getComment());
        holder.userText.setText(comment.getUser());

        if (comment.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(comment.getTimestamp()));
            holder.timestampText.setText(formattedDate);
        } else {
            holder.timestampText.setText("");
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
