package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private GeminiClient geminiClient;
    private EditText topicInput;
    private Button generateButton;
    private ProgressBar loadingIndicator;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topicInput = findViewById(R.id.topic_input);
        generateButton = findViewById(R.id.generate_quiz_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingOverlay = findViewById(R.id.loading_overlay);
        geminiClient = new GeminiClient(this);

        // 🔽 Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    return true;
                case R.id.nav_settings:
                    startActivity(new Intent(MainActivity.this, ProfileSettingsActivity.class));
                    return true;
                case R.id.nav_saved_notes:
                    startActivity(new Intent(MainActivity.this, SavedNotesActivity.class));
                    return true;
                case R.id.nav_saved_results:
                    startActivity(new Intent(MainActivity.this, ResultsActivity.class));
                    return true;
                case R.id.nav_saved_quizzes:
                    startActivity(new Intent(MainActivity.this, SavedQuizzesActivity.class));
                    return true;
            }
            return false;
        });

        // 🔽 Generate Quiz Button Logic
        generateButton.setOnClickListener(v -> {
            String userInput = topicInput.getText().toString().trim();
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
                return;
            }

            topicInput.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingOverlay.setVisibility(View.VISIBLE);
            generateButton.setEnabled(false);

            geminiClient.generate(userInput, new GeminiClient.Callback() {
                @Override
                public void onSuccess(String jsonResult) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                        intent.putExtra("RESULT_JSON", jsonResult);
                        startActivity(intent);
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
        loadingOverlay.setVisibility(View.GONE);
        generateButton.setEnabled(true);
    }
}
