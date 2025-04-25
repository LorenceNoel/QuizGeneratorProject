package com.example.quizgeneratorproject;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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
    }
}
