package com.example.quizgeneratorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserSettings";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String NOTIFICATIONS_KEY = "notifications";

    private Switch darkModeSwitch;
    private Switch notificationsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings_activity);

        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        notificationsSwitch = findViewById(R.id.notifications_switch);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(DARK_MODE_KEY, false);
        boolean notifyOn = prefs.getBoolean(NOTIFICATIONS_KEY, true);

        darkModeSwitch.setChecked(isDark);
        notificationsSwitch.setChecked(notifyOn);
        applyDarkMode(isDark);

        darkModeSwitch.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(DARK_MODE_KEY, checked).apply();
            applyDarkMode(checked);
        });
        notificationsSwitch.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(NOTIFICATIONS_KEY, checked).apply();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // ← Highlight the Settings tab on startup
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                // Already on Settings—just keep it checked
                return true;
            } else if (id == R.id.nav_saved_notes) {
                startActivity(new Intent(this, SavedNotesActivity.class));
                return true;
            } else if (id == R.id.nav_saved_quizzes) {
                startActivity(new Intent(this, SavedQuizzesActivity.class));
                return true;
            }
            return false;
        });
    }

    private void applyDarkMode(boolean enable) {
        AppCompatDelegate.setDefaultNightMode(
                enable
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
