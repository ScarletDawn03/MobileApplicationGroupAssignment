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
        // Set the PDF name
        String pdfName = course.getCr_pdfName();  // Assuming the field is cr_pdfName
        if (pdfName != null && !pdfName.isEmpty()) {
            holder.pdfName.setText(pdfName);  // Set the PDF name in the TextView
        } else {
            holder.pdfName.setText("No PDF Name");  // Set default text if PDF name is missing
        }

        // Set the created_at field
        String createdAt = course.getCreated_at();  // Assuming the field is created_at
        if (createdAt != null && !createdAt.isEmpty()) {
            holder.createdAt.setText("Created At: " + createdAt);  // Display "Created At"
        } else {
            holder.createdAt.setText("Creation date not available");
        }

        // Set the created_by field
        String createdBy = course.getCreated_by();  // Assuming the field is created_by
        if (createdBy != null && !createdBy.isEmpty()) {
            holder.createdBy.setText("Created By: " + createdBy);  // Display "Created By"
        } else {
            holder.createdBy.setText("Creator not available");
        }

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
        TextView pdfName, createdAt, createdBy;

        public ViewHolder(View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);
            createdAt = itemView.findViewById(R.id.created_at);
            createdBy = itemView.findViewById(R.id.created_by);
        }
    }
}
