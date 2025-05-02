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



/**
 * Adapter class for displaying comments in a RecyclerView.
 * Each comment includes the message, user email, and timestamp.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<CommentModel> commentList;


    /**
     * Constructor for ChatAdapter.
     *
     * @param commentList A list of CommentModel objects to be displayed.
     */
    public ChatAdapter(List<CommentModel> commentList) {
        this.commentList = commentList;
    }

    /**
     * ViewHolder class that holds references to the views in each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, userText, timestampText;
        public ViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            userText = itemView.findViewById(R.id.user_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }
    }

    /**
     * Inflates the item layout and returns a new ViewHolder instance.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder object.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }


    /**
     * Binds data to the ViewHolder for the given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
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

    /**
     * Returns the current list size.
     *
     * @return Number of items in the comment list.
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

}
