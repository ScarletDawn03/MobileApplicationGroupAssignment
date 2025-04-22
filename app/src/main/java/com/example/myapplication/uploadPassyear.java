package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class uploadPassyear extends AppCompatActivity {
    private TextView upl_code,upl_name,show_uplname;
    private Spinner upl_category;
    private TextInputLayout upl_desc_layout;
    private TextInputEditText upl_desc;
    private Button upl_btn,selectFile_btn,rst_uplbtn;
    private String fileName,fileOrgName,fileExtName,code,name,category,desc;
    private Uri pdfuri;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference coursesRef = database.getReference("courses");

    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            //Unable use startActivityForResult directly as it deprecated
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        pdfuri = result.getData().getData();
                        if (pdfuri != null) {
                            Cursor returnCursor = getContentResolver().query(pdfuri, null, null, null, null);
                            if (returnCursor != null && returnCursor.moveToFirst()) {
                                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                int filesize = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                                long filesizeInByte = returnCursor.getLong(filesize);
                                long maxfileSize = 15 * 1024 * 1024;
                                fileOrgName = returnCursor.getString(nameIndex);
                                returnCursor.close();
                                fileExtName = fileOrgName.substring(0,fileOrgName.length()-4);

                                //Check size of file uploaded
                                if(filesizeInByte > maxfileSize){
                                    Toast.makeText(uploadPassyear.this, "Only PDF file below 15MB can be uploaded!", Toast.LENGTH_LONG).show();
                                    pdfuri = null;
                                    return;
                                }
                            }
                            // Display information (you can remove this or modify as needed)
                            String info = "File Name: " + fileOrgName + "\n";
                            if(!fileOrgName.isEmpty()){
                                show_uplname.setText("Selected File:" + fileOrgName);
                            }

                            Toast.makeText(uploadPassyear.this, info, Toast.LENGTH_LONG).show();

                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_passyear);
        getIDForViews();
        setFormEnabled(true);

        rst_uplbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfuri=null;
                show_uplname.setText("");
                Toast.makeText(uploadPassyear.this, "Remove selected file", Toast.LENGTH_LONG).show();
            }
        });

        upl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code = upl_code.getText().toString().trim();
                name = upl_name.getText().toString().trim();
                category = upl_category.getSelectedItem().toString();
                desc = upl_desc.getText().toString().trim();

                if (pdfuri == null) {
                    Toast.makeText(uploadPassyear.this, "Please select a pdf first", Toast.LENGTH_LONG).show();
                    return;
                }

                if (code.isEmpty()) {
                    upl_code.setError("Please enter course code");
                    return;
                }

                if (name.isEmpty()) {
                    upl_name.setError("Please enter course name");
                    return;
                }

                if(category.equals("Select type of assessment")){
                    ((TextView)upl_category.getSelectedView()).setError("");
                    return;
                }

                if (desc.isEmpty()) {
                    upl_desc.setError("Please enter description");
                    return;
                }
                Toast.makeText(uploadPassyear.this,"Uploading...Please wait... ", Toast.LENGTH_SHORT).show();
                setFormEnabled(false);
                uploadPDFAndSaveData();
            }
        });

    }

    public void goHome(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void addCourseToDB(String code, String name, String category, String desc,String filename,String pdfUrl){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        //create hashmap
        HashMap<String,Object> courseHashmap = new HashMap<>();
        courseHashmap.put("cr_code",code);
        courseHashmap.put("cr_name",name);
        courseHashmap.put("cr_category",category);
        courseHashmap.put("cr_desc",desc);
        courseHashmap.put("cr_pdfName",filename);
        courseHashmap.put("cr_pdfUrl",pdfUrl);
        courseHashmap.put("created_at", currentDate);


        //something like primary key
        String key = coursesRef.push().getKey();
        courseHashmap.put("key",key);

        coursesRef.child(key).setValue(courseHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(uploadPassyear.this,"Uploaded Successfully", Toast.LENGTH_SHORT).show();
                upl_code.setText("");
                upl_name.setText("");
                upl_desc.setText("");
                show_uplname.setText("");
                upl_category.setSelection(0);
                pdfuri = null;
                setFormEnabled(true);

            }
        });
    }

    public void getIDForViews(){
        upl_code = findViewById(R.id.upload_code);
        upl_name = findViewById(R.id.upload_name);
        upl_category = findViewById(R.id.upload_category);
        upl_desc_layout = findViewById(R.id.upload_desc_layout);
        upl_desc = upl_desc_layout.findViewById(R.id.upload_desc);
        upl_btn = findViewById(R.id.upload_btn);
        show_uplname = findViewById(R.id.showfilename);
        selectFile_btn = findViewById(R.id.selectFile);
        rst_uplbtn = findViewById(R.id.rst_uplbtn);
    }

    private void setFormEnabled(boolean enabled) {
        upl_code.setEnabled(enabled);
        upl_name.setEnabled(enabled);
        upl_category.setEnabled(enabled);
        upl_desc_layout.setEnabled(enabled);
        upl_btn.setEnabled(enabled);
        selectFile_btn.setEnabled(enabled);
    }

    public void openFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }


    private void uploadPDFAndSaveData() {
        if (pdfuri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
            String timeStamp = sdf.format(new Date());
            fileName =  fileExtName+"_"+timeStamp+ ".pdf";
            StorageReference fileRef = storageRef.child("pdfs/" + fileName);

            fileRef.putFile(pdfuri).addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String pdfUrl = uri.toString();
                            addCourseToDB(code,name,category,desc,fileName,pdfUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
        }
    }

}