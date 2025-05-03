package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MenuItem;
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

import android.Manifest; // for Manifest.permission.POST_NOTIFICATIONS
import android.preference.PreferenceManager;// for retrieve notification preferences
import android.content.SharedPreferences; // for shared preferences

public class uploadPassyear extends AppCompatActivity {
    private TextView upl_code,upl_name,show_uplname;
    private Spinner upl_category;
    private TextInputLayout upl_desc_layout;
    private TextInputEditText upl_desc;
    private Button upl_btn,selectFile_btn,rst_uplbtn;
    private String fileName,fileOrgName,fileExtName,fileType,code,name,category,desc;
    private Uri pdfuri;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference coursesRef = database.getReference("courses");

    //For notification permission check and function
    private static final int REQ_POST_NOTIFICATIONS = 1001;
    private Notification pendingNotification;  // to hold the built notification
    private Toast toast;
    //The luncher for handling selection of document file
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
                                //Get the information of selected document file
                                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                int filesize = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                                long filesizeInByte = returnCursor.getLong(filesize);
                                //Set max uploaded file size
                                long maxfileSize = 15 * 1024 * 1024;

                                fileOrgName = returnCursor.getString(nameIndex);
                                int dotIndex = fileOrgName.lastIndexOf(".");
                                returnCursor.close();
                                fileExtName = fileOrgName.substring(0,dotIndex); //Dynamically retreive file name
                                fileType = getContentResolver().getType(pdfuri);  //Get mimeType

                                //Check size of file uploaded
                                if(filesizeInByte > maxfileSize){
                                    Toast.makeText(uploadPassyear.this, "Only document file below 15MB can be uploaded!", Toast.LENGTH_LONG).show();
                                    pdfuri = null;
                                    return;
                                }
                                // Display information
                                String info = "File Name: " + fileOrgName + "\n";
                                if(!fileOrgName.isEmpty()){
                                    show_uplname.setText("Selected File:" + fileOrgName);
                                }
                            }
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

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //For API level 26 above phone permission
        // Create notification channel for local notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    "my_uploads",                     // channel ID
                    "My Upload Notifications",        // user-visible name
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            chan.setDescription("Notifies you about your own uploads");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(chan);
        }

        //Remove selected file when click 'Reset' button
        rst_uplbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfuri=null;
                show_uplname.setText("");
                Toast.makeText(uploadPassyear.this, "Remove selected file", Toast.LENGTH_LONG).show();
            }
        });

        //Limit user to input maximum 150 characters into description of uploaded document
        upl_desc.addTextChangedListener(new TextWatcher() {
            private final int max_length =150;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() > max_length){
                        upl_desc.setText(s.subSequence(0,max_length));
                        upl_desc.setSelection(max_length); // move cursor to end
                        Toast.makeText(uploadPassyear.this, "Max 150 characters allowed", Toast.LENGTH_LONG).show();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Checking and submit input to database when clicking submit button
        upl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code = upl_code.getText().toString().trim().replaceAll("\\s+", "").toUpperCase();
                name = upl_name.getText().toString().trim();
                category = upl_category.getSelectedItem().toString();
                desc = upl_desc.getText().toString().trim();

                if (pdfuri == null) {
                    Toast.makeText(uploadPassyear.this, "Please select a document first", Toast.LENGTH_LONG).show();
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

                //Display notification of updating and unable user to perform any action on the form during submission
                toast = Toast.makeText(uploadPassyear.this, "Uploading...Please wait... ", Toast.LENGTH_SHORT);
                toast.show();
                setFormEnabled(false);
                uploadFileAndSaveData();
            }
        });



    }

    //Check for permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Double-check we really have the permission (satisfies lint)
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

                    // now that we have permission, finally show the notification
                    if (pendingNotification != null) {
                        NotificationManagerCompat.from(this)
                                .notify((int) System.currentTimeMillis(), pendingNotification);
                    }
                }
            } else {
                Toast.makeText(this,
                        "Cannot show notification without permission",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Upload input data into database
    private void addCourseToDB(String code, String name, String category, String desc,String filename,String pdfUrl,String author){
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
        courseHashmap.put("created_by",author);

        // Add the 'likes' attribute initialized to 0 and 'liked_by' as empty map
        courseHashmap.put("likes", 0);
        courseHashmap.put("liked_by", new HashMap<String, Boolean>());


        //Something like primary key
        String key = coursesRef.push().getKey();
        courseHashmap.put("key",key);

        //Reset form after submitting successfully
        coursesRef.child(key).setValue(courseHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                toast.cancel();
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

    //Link components
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

    //Enable or unable user to edit form
    private void setFormEnabled(boolean enabled) {
        upl_code.setEnabled(enabled);
        upl_name.setEnabled(enabled);
        upl_category.setEnabled(enabled);
        upl_desc_layout.setEnabled(enabled);
        upl_btn.setEnabled(enabled);
        selectFile_btn.setEnabled(enabled);
        rst_uplbtn.setEnabled(enabled);
    }

    //Open storage and limit the file type in selecting document file
    public void openFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //Set the limit which only can upload .pdf, .doc, and .docx
        String[] mimeType = {"application/pdf","application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
        filePickerLauncher.launch(intent);
    }

    //
    private void uploadFileAndSaveData() {
        if (pdfuri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
            String timeStamp = sdf.format(new Date()); //Get date and time for 'created_at' attribute
            if("application/msword".equals(fileType)){ //Set the file name to be stored in database
                fileName =  fileExtName+"_"+timeStamp+ ".doc";
            }else if("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(fileType)){
                fileName =  fileExtName+"_"+timeStamp+ ".docx";
            }else if("application/pdf".equals(fileType)){
                fileName =  fileExtName+"_"+timeStamp+ ".pdf";
            }

            //The location of storing uploaded document
            StorageReference fileRef = storageRef.child("pdfs/" + fileName);

            //Retrieve user's email as author of uploaded document
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String userEmail = sharedPreferences.getString("user_email", null);

            //Upload file to Firebase storage
            fileRef.putFile(pdfuri).addOnSuccessListener(taskSnapshot -> {
                        //Get URL of uploaded file
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String pdfUrl = uri.toString();
                            //Upload file's information into database
                            addCourseToDB(code,name,category,desc,fileName,pdfUrl,userEmail);
                            //Reset variable for storing file name
                            fileExtName = null;
                        });

                        // Save successful update to SharedPreferences
                        String newUpdate = code + "," + category + " - " + "Upload successful for " + fileName ;
                        saveUpdateToPreferences(newUpdate);

                        // Get the user’s saved prefs
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        boolean allowNewUploads = prefs.getBoolean("pref_notify_new_uploads", true);

                        if (allowNewUploads) {
                            // Build the notification
                            NotificationCompat.Builder notif = new NotificationCompat.Builder(this, "my_uploads")
                                    .setSmallIcon(R.drawable.upload)
                                    .setContentTitle("Upload Complete")
                                    .setContentText("You just uploaded: " + fileName)
                                    .setAutoCancel(true);

                            // Check runtime permission (Android 13+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    NotificationManagerCompat.from(this)
                                            .notify((int) System.currentTimeMillis(), notif.build());
                                } else {
                                    pendingNotification = notif.build();
                                    ActivityCompat.requestPermissions(
                                            this,
                                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                            REQ_POST_NOTIFICATIONS
                                    );
                                }
                            } else {
                                NotificationManagerCompat.from(this)
                                        .notify((int) System.currentTimeMillis(), notif.build());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please select a document first", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to save updates to SharedPreferences
    private void saveUpdateToPreferences(String newUpdate) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String existingUpdates = sharedPreferences.getString("update_list", "");

        // Add new update to existing updates
        String updatedUpdates = existingUpdates + "\n" + newUpdate;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update_list", updatedUpdates);
        editor.apply();
    }

    //The button's event handler for back to the previous page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}