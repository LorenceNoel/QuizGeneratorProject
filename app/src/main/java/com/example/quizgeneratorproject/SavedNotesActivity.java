package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SavedNotesActivity extends AppCompatActivity {

    private LinearLayout notesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        notesContainer = findViewById(R.id.notes_container);

        String[] savedNotes = {
                "Math Quiz: Algebra, Equations, Fractions...",
                "Science Quiz: Photosynthesis, DNA, Atoms...",
                "History Quiz: WW2, Industrial Revolution..."
        };

        for (String note : savedNotes) {
            TextView noteBox = new TextView(this);
            noteBox.setText(note);
            noteBox.setTextSize(16);
            noteBox.setTextColor(getResources().getColor(R.color.white));
            noteBox.setBackgroundResource(R.drawable.note_card_border);
            noteBox.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 32);
            noteBox.setLayoutParams(params);

            notesContainer.addView(noteBox);
        }

        // 🔻 Add this block for BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_saved_notes); // highlight current tab

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_results:
                    startActivity(new Intent(this, NotesResultsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_saved_notes:
                    return true; // Already here
                case R.id.nav_saved_quizzes:
                    startActivity(new Intent(this, SavedQuizzesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_settings:
                    startActivity(new Intent(this, ProfileSettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }
}
