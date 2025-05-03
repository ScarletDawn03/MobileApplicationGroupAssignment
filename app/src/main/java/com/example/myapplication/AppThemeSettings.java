package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class AppThemeSettings extends AppCompatActivity {
    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark;
    private Button btnSave;
    private SharedPreferences prefs;

    /** this function is to set background theme whether is light mode or dark mode**/
    /** By default it is light mode for user **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        rgTheme = findViewById(R.id.rgTheme);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);
        btnSave = findViewById(R.id.btnSaveTheme);

        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        if (darkMode) rbDark.setChecked(true);
        else rbLight.setChecked(true);

        btnSave.setOnClickListener(v -> {
            boolean useDark = (rgTheme.getCheckedRadioButtonId() == R.id.rbDark);
            prefs.edit().putBoolean("pref_dark_mode", useDark).apply();

            AppCompatDelegate.setDefaultNightMode(
                    useDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(this, "Update theme successfully", Toast.LENGTH_SHORT).show();
            recreate(); // recreate to apply theme change
        });
    }
}
