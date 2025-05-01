package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyUploadsAdapter extends RecyclerView.Adapter<MyUploadsAdapter.UploadViewHolder> {

    private List<UploadItem> uploadList;
    private Context context;

    public MyUploadsAdapter(List<UploadItem> uploadList, Context context) {
        this.uploadList = uploadList;
        this.context = context;

    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upload, parent, false);
        return new UploadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        UploadItem item = uploadList.get(position);
        holder.tvCourseCode.setText(item.getCr_code());
        holder.tvCourseName.setText(item.getCr_name());
        holder.tvCategory.setText(item.getCr_category());
        holder.tvPdfName.setText(item.getCr_pdfName());
        holder.tvCreatedAt.setText(item.getCreated_at());

        holder.btnViewPdf.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(item.getCr_pdfUrl()));
            context.startActivity(intent);
        });

        holder.btnEditPdf.setOnClickListener(view ->{
            Intent intent = new Intent(context, EditMyUpload.class);
            intent.putExtra("cr_filename", item.getCr_pdfName());
            intent.putExtra("cr_code", item.getCr_code());
            intent.putExtra("cr_name", item.getCr_name());
            intent.putExtra("cr_category", item.getCr_category());
            intent.putExtra("cr_desc", item.getCr_desc());
            intent.putExtra("key", item.getKey());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(view -> {
            Log.d("DeleteButton", "Delete button clicked");
            if (context instanceof MyUploadsActivity) {
                ((MyUploadsActivity) context).deleteUpload(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploadList.size();
    }

    static class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseCode, tvCourseName, tvCategory, tvCreatedAt, tvPdfName, deleteButton;
        Button btnViewPdf,btnEditPdf;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvPdfName=itemView.findViewById(R.id.tvPdfName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);
            btnEditPdf = itemView.findViewById(R.id.btnEditPdf);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
