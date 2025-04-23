package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<SourceDocumentModelClass> courseList;

    public CourseAdapter(List<SourceDocumentModelClass> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (courseList.isEmpty()) {
            // Handle empty list (you could show a placeholder if desired)
            return;
        }

        SourceDocumentModelClass course = courseList.get(position);
        holder.title.setText(course.getCr_name());
        holder.category.setText(course.getCr_category());

        // Set up a listener to open the PDF URL when clicked
        holder.itemView.setOnClickListener(v -> {
            String pdfUrl = course.getCr_pdfUrl();
            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                v.getContext().startActivity(browserIntent);
            } else {
                Toast.makeText(v.getContext(), "No PDF available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.course_title);
            category = itemView.findViewById(R.id.course_category);
        }
    }
}
