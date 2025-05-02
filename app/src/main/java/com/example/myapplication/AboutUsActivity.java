package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


/**
 * AboutUsActivity displays information about the application or the development team along side the naming convention of our files.
 */
public class AboutUsActivity extends AppCompatActivity {


    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     *                           Otherwise, it is null.
     */

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

    }

    /**
     * Handles the selection of menu items.
     *
     * @param item The menu item that was selected.
     * @return true if the item selection was handled; otherwise, call the superclass method.
     */
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
