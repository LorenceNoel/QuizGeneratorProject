package com.example.quizgeneratorproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;

public class ProfileSettingsActivity extends AppCompatActivity {

    static final String CHANNEL_ID = "study_reminders";
    private static final String CHANNEL_NAME = "Study Session Reminders";
    private static final int REQUEST_NOTIF_PERMISSION = 1001;

    private static final String PREFS_NAME = "UserSettings";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String NOTIFICATIONS_KEY = "notifications";
    private static final String KEY_HOUR = "notify_hour";
    private static final String KEY_MINUTE = "notify_minute";
    private static final String KEY_DAY_PREFIX = "day_";       // 0=Sun … 6=Sat
    private static final int WEEK_MS = 7 * 24 * 60 * 60 * 1000;

    private Switch darkModeSwitch;
    private Switch notificationsSwitch;
    private MaterialButton logoutButton;
    private LinearLayout scheduleContainer;
    private CheckBox[] dayCheckBoxes;
    private TextView timeValue;
    private SharedPreferences prefs;

    /**
     * Static helpers for reboot scheduling
     **/
    public static void scheduleAllAlarmsStatic(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int hour = prefs.getInt(KEY_HOUR, 8);
        int minute = prefs.getInt(KEY_MINUTE, 0);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Calendar now = Calendar.getInstance();
        cancelAllAlarmsStatic(ctx);

        for (int i = 0; i < 7; i++) {
            if (!prefs.getBoolean(KEY_DAY_PREFIX + i, false)) continue;

            Calendar cal = (Calendar) now.clone();
            cal.set(Calendar.DAY_OF_WEEK, i + 1);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            if (cal.before(now)) cal.add(Calendar.MILLISECOND, WEEK_MS);

            Intent intent = new Intent(ctx, ReminderReceiver.class)
                    .putExtra("day", i);
            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx,
                    100 + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), WEEK_MS, pi);
        }
    }

    public static void cancelAllAlarmsStatic(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 7; i++) {
            Intent intent = new Intent(ctx, ReminderReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx,
                    100 + i,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) am.cancel(pi);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings_activity);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        logoutButton = findViewById(R.id.logout_button);
        scheduleContainer = findViewById(R.id.schedule_container);
        timeValue = findViewById(R.id.time_value);

        dayCheckBoxes = new CheckBox[]{
                findViewById(R.id.cb_sun),
                findViewById(R.id.cb_mon),
                findViewById(R.id.cb_tue),
                findViewById(R.id.cb_wed),
                findViewById(R.id.cb_thu),
                findViewById(R.id.cb_fri),
                findViewById(R.id.cb_sat)
        };

        // restore prefs
        boolean isDark = prefs.getBoolean(DARK_MODE_KEY, false);
        boolean notifyOn = prefs.getBoolean(NOTIFICATIONS_KEY, true);
        int savedHour = prefs.getInt(KEY_HOUR, 8);
        int savedMinute = prefs.getInt(KEY_MINUTE, 0);

        darkModeSwitch.setChecked(isDark);
        notificationsSwitch.setChecked(notifyOn);
        applyDarkMode(isDark);

        timeValue.setText(String.format(Locale.getDefault(), "%02d:%02d", savedHour, savedMinute));
        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i].setChecked(prefs.getBoolean(KEY_DAY_PREFIX + i, false));
        }
        scheduleContainer.setVisibility(notifyOn ? View.VISIBLE : View.GONE);

        createNotificationChannel();
        requestNotificationPermission();

        // dark mode toggle
        darkModeSwitch.setOnCheckedChangeListener((v, checked) -> {
            prefs.edit().putBoolean(DARK_MODE_KEY, checked).apply();
            applyDarkMode(checked);
        });

        // notifications toggle
        notificationsSwitch.setOnCheckedChangeListener((v, checked) -> {
            prefs.edit().putBoolean(NOTIFICATIONS_KEY, checked).apply();
            scheduleContainer.setVisibility(checked ? View.VISIBLE : View.GONE);

            if (!checked) {
                cancelAllAlarms();
                return;
            }
            // now checked == true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIF_PERMISSION
                );
            } else {
                scheduleAllAlarms();
            }
        });

        // day-of-week toggles
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            dayCheckBoxes[i].setOnCheckedChangeListener((cb, checked) -> {
                prefs.edit().putBoolean(KEY_DAY_PREFIX + idx, checked).apply();
                if (notificationsSwitch.isChecked()) scheduleAllAlarms();
            });
        }

        // time picker
        timeValue.setOnClickListener(v -> {
            new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        prefs.edit()
                                .putInt(KEY_HOUR, hour)
                                .putInt(KEY_MINUTE, minute)
                                .apply();
                        timeValue.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                        if (notificationsSwitch.isChecked()) scheduleAllAlarms();
                    },
                    savedHour, savedMinute, true
            ).show();
        });

        // logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        // bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_saved_notes)
                startActivity(new Intent(this, SavedNotesActivity.class));
            else if (id == R.id.nav_saved_quizzes)
                startActivity(new Intent(this, SavedQuizzesActivity.class));
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(req, perms, grants);
        if (req == REQUEST_NOTIF_PERMISSION) {
            boolean ok = grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED;
            if (!ok) {
                Toast.makeText(this, "Notification permission denied – disabling reminders", Toast.LENGTH_SHORT).show();
                notificationsSwitch.setChecked(false);
            } else if (notificationsSwitch.isChecked()) {
                scheduleAllAlarms();
            }
        }
    }

    private void applyDarkMode(boolean enable) {
        AppCompatDelegate.setDefaultNightMode(
                enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            );
            ch.setDescription("Reminders for your study sessions");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(ch);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIF_PERMISSION
            );
        }
    }

    private void scheduleAllAlarms() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar now = Calendar.getInstance();
        int hour = prefs.getInt(KEY_HOUR, 8);
        int minute = prefs.getInt(KEY_MINUTE, 0);

        cancelAllAlarms();
        for (int i = 0; i < 7; i++) {
            if (!prefs.getBoolean(KEY_DAY_PREFIX + i, false)) continue;

            Calendar cal = (Calendar) now.clone();
            cal.set(Calendar.DAY_OF_WEEK, i + 1);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            if (cal.before(now)) cal.add(Calendar.MILLISECOND, WEEK_MS);

            Intent intent = new Intent(this, ReminderReceiver.class).putExtra("day", i);
            PendingIntent pi = PendingIntent.getBroadcast(
                    this,
                    100 + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), WEEK_MS, pi);
        }
    }

    private void cancelAllAlarms() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 7; i++) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    this,
                    100 + i,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) am.cancel(pi);
        }
    }
}
