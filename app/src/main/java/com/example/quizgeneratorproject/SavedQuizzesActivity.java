package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SavedQuizzesActivity extends AppCompatActivity {

    Button viewQuizButton, retakeQuizButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_quizzes);

        viewQuizButton = findViewById(R.id.view_quiz_button);
        retakeQuizButton = findViewById(R.id.retake_quiz_button);

        viewQuizButton.setOnClickListener(v -> {
            // Navigate or show details of quiz
            Toast.makeText(this, "Viewing saved quiz...", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, ViewQuizActivity.class));
        });

        retakeQuizButton.setOnClickListener(v -> {
            // Retake the quiz
            Toast.makeText(this, "Retaking quiz...", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, QuizGeneratorActivity.class));
        });
    }
}
