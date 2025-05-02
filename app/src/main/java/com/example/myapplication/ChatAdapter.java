package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<CommentModel> commentList;

    public ChatAdapter(List<CommentModel> commentList) {
        this.commentList = commentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, userText;

        public ViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            userText = itemView.findViewById(R.id.user_text);
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
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
