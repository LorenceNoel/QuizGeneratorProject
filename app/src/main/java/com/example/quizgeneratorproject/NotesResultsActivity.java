package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotesResultsActivity extends AppCompatActivity {

    TextView notesTextView;
    Button saveButton, shareButton, exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_results);

        notesTextView = findViewById(R.id.generated_notes);
        saveButton = findViewById(R.id.save_button);
        shareButton = findViewById(R.id.share_button);
        exportButton = findViewById(R.id.export_button);

        // Simulate setting the notes
        String aiGeneratedNotes = "• Topic: Java Basics\n• Key Concepts: Variables, Loops, Conditions\n• Summary: Java is an object-oriented language...";
        notesTextView.setText(aiGeneratedNotes);

        saveButton.setOnClickListener(v -> {
            // TODO: Implement actual save logic to local storage or database
            Toast.makeText(this, "Notes saved successfully!", Toast.LENGTH_SHORT).show();
        });

        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, notesTextView.getText().toString());
            startActivity(Intent.createChooser(shareIntent, "Share Notes Via"));
        });

        exportButton.setOnClickListener(v -> {
            // TODO: Implement export to PDF or file
            Toast.makeText(this, "Exported as PDF (mock action)", Toast.LENGTH_SHORT).show();
        });
    }
}
