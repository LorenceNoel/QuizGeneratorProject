package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, ProfileSettingsActivity.class));
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


        generateButton.setOnClickListener(v -> {
            String userInput = topicInput.getText().toString().trim();
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
                return;
            }

            topicInput.setText("");
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.VISIBLE);
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
        loadingOverlay.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        generateButton.setEnabled(true);
    }
}
