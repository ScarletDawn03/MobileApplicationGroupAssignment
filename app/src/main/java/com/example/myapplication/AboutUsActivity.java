package com.example.myapplication;


import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        // Set up the Toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);  // Make sure you have a Toolbar with this ID in your layout
        setSupportActionBar(toolbar);

        // Set the back button (home) in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // (Optional) You can findViewById TextViews here if you want to update them dynamically later
        // Example:
        // TextView title = findViewById(R.id.titleText);
        // title.setText("New About Us Title");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check if the item is the "home" button (back button)
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to the main menu or previous activity
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
