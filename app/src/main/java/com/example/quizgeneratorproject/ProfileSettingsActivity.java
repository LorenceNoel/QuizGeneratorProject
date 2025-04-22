package com.example.quizgeneratorproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ProfileSettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private Switch notificationsSwitch;

    private static final String PREFS_NAME = "UserSettings";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String NOTIFICATIONS_KEY = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings_activity);

        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        notificationsSwitch = findViewById(R.id.notifications_switch);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkModeEnabled = preferences.getBoolean(DARK_MODE_KEY, false);
        boolean areNotificationsEnabled = preferences.getBoolean(NOTIFICATIONS_KEY, true);

        darkModeSwitch.setChecked(isDarkModeEnabled);
        notificationsSwitch.setChecked(areNotificationsEnabled);


        applyDarkMode(isDarkModeEnabled);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            applyDarkMode(isChecked);
        });

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(NOTIFICATIONS_KEY, isChecked).apply();

        });
    }

    private void applyDarkMode(boolean enable) {
        AppCompatDelegate.setDefaultNightMode(
                enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
