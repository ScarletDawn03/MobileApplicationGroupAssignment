package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EditMyUpload extends AppCompatActivity {
    private String filename,c_code,c_name,c_category,c_desc,c_key,code,name,category,desc;
    private TextView myupl_code,myupl_name,show_myuplname;
    private Spinner myupl_category;
    private TextInputLayout myupl_desc_layout;
    private TextInputEditText myupl_desc;
    private Button myupl_btn;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference coursesRef = database.getReference("courses");
    private Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_upload);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Store information of selected file into variables
        filename = getIntent().getStringExtra("cr_filename");
        c_code = getIntent().getStringExtra("cr_code");
        c_name = getIntent().getStringExtra("cr_name");
        c_category = getIntent().getStringExtra("cr_category");
        c_desc = getIntent().getStringExtra("cr_desc");
        c_key = getIntent().getStringExtra("key");
        setIDElement();
        setFormEnabled(true);

        //Limit user to input maximum 150 characters into description of uploaded document
        myupl_desc.addTextChangedListener(new TextWatcher() {
            private final int max_length = 150;
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
              if(s.length() > max_length){
                  myupl_desc.setText(s.subSequence(0,max_length));
                  myupl_desc.setSelection(max_length); // move cursor to end
                  Toast.makeText(EditMyUpload.this, "Max 150 characters allowed", Toast.LENGTH_LONG).show();
              }
          }

          @Override
          public void afterTextChanged(Editable s) {

          }
        });

        //Perform checking any invalid input when clicking update button
        myupl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = myupl_code.getText().toString().trim().replaceAll("\\s+", "").toUpperCase();
                name = myupl_name.getText().toString().trim();
                category = myupl_category.getSelectedItem().toString();
                desc = myupl_desc.getText().toString().trim();


                if (code.isEmpty()) {
                    myupl_code.setError("Please enter course code");
                    return;
                }

                if (name.isEmpty()) {
                    myupl_name.setError("Please enter course name");
                    return;
                }

                if (category.equals("Select type of assessment")) {
                    ((TextView) myupl_category.getSelectedView()).setError("");
                    return;
                }

                if (desc.isEmpty()) {
                    myupl_desc.setError("Please enter description");
                    return;
                }

                //Display notification of updating and unable user to perform any action on the form during submission
                toast = Toast.makeText(EditMyUpload.this, "Updating...Please wait... ", Toast.LENGTH_SHORT);
                toast.show();
                setFormEnabled(false);
                updateData();
            }
        });

    }

    //Handle uploading modification of information into database
    private void updateData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        HashMap<String, Object> updatedCourse = new HashMap<>();
        updatedCourse.put("cr_code", code);
        updatedCourse.put("cr_name", name);
        updatedCourse.put("cr_category", category);
        updatedCourse.put("cr_desc", desc);
        updatedCourse.put("updated_at", currentDate); // You might want a separate "updated_at" field

        // update only the fields provided above
        coursesRef.child(c_key).updateChildren(updatedCourse).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Display notification regarding updated successfully and enable user to edit form
                toast.cancel();
                Toast.makeText(EditMyUpload.this,"Updated Successfully", Toast.LENGTH_SHORT).show();
                setFormEnabled(true);
            }
        });


    }

    //Link components
    private void setIDElement(){
        show_myuplname = findViewById(R.id.showmyfilename);
        myupl_code = findViewById(R.id.myupload_code);
        myupl_name = findViewById(R.id.myupload_name);
        myupl_category = findViewById(R.id.myupload_category);
        myupl_desc = findViewById(R.id.myupload_desc);
        myupl_desc_layout = findViewById(R.id.myupload_desc_layout);
        myupl_btn = findViewById(R.id.myupload_subbtn);

        //Auto fill the form according the selected document
        show_myuplname.setText(filename);
        myupl_code.setText(c_code);
        myupl_name.setText(c_name);
        ArrayAdapter<CharSequence> sadapter = (ArrayAdapter<CharSequence>) myupl_category.getAdapter();

        int choicesPosition = sadapter.getPosition(c_category);
        if(choicesPosition >=0){
            myupl_category.setSelection(choicesPosition);

        }
        myupl_desc.setText(c_desc);

    }

    //Enable or unable user to edit form
    private void setFormEnabled(boolean enabled) {
        myupl_code.setEnabled(enabled);
        myupl_name.setEnabled(enabled);
        myupl_category.setEnabled(enabled);
        myupl_desc_layout.setEnabled(enabled);
        myupl_desc.setEnabled(enabled);
        myupl_btn.setEnabled(enabled);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // This handles the default back behavior
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