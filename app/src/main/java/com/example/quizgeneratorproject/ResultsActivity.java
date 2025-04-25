package com.example.quizgeneratorproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;

public class ResultsActivity extends AppCompatActivity {
    private TextView notesView, quizView;
    private Button saveNotesBtn, saveQuizBtn;
    private String notesContent, quizContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        notesView = findViewById(R.id.notes_view);
        quizView = findViewById(R.id.quiz_view);
        saveNotesBtn = findViewById(R.id.save_notes_button);
        saveQuizBtn = findViewById(R.id.save_quiz_button);

        // ensure text is visible
        int white = ContextCompat.getColor(this, R.color.white);
        notesView.setTextColor(white);
        quizView.setTextColor(white);

        String json = getIntent().getStringExtra("RESULT_JSON");
        parseAndDisplay(json);

        saveNotesBtn.setOnClickListener(v -> saveContent(notesContent, "notes.txt"));
        saveQuizBtn.setOnClickListener(v -> saveContent(quizContent, "quiz.txt"));
    }

    private void parseAndDisplay(String json) {
        try {
            JSONObject root = new JSONObject(json);

            // === Notes Section (unchanged) ===
            JSONObject notes = root.getJSONObject("notes");
            StringBuilder nb = new StringBuilder();
            nb.append(notes.optString("title")).append("\n\n");
            nb.append(notes.optString("introduction")).append("\n\n");
            for (int i = 1; i <= 3; i++) {
                String key = "section_" + i;
                if (!notes.has(key)) continue;
                JSONObject sec = notes.getJSONObject(key);
                nb.append(sec.optString("title")).append("\n").append(sec.optString("content")).append("\n");
                if (sec.has("subtopics")) {
                    JSONArray subs = sec.getJSONArray("subtopics");
                    if (subs.length() > 0) {
                        JSONObject sub = subs.getJSONObject(0);
                        nb.append("• ").append(sub.optString("title")).append(": ").append(sub.optString("content")).append("\n");
                    }
                }
                if (sec.has("examples")) {
                    JSONArray exs = sec.getJSONArray("examples");
                    if (exs.length() > 0) {
                        nb.append("- Example: ").append(exs.getString(0)).append("\n");
                    }
                }
                nb.append("\n");
            }
            nb.append(notes.optString("conclusion")).append("\n");
            notesContent = nb.toString().trim();

            // === Quiz Section with options and answer key ===
            JSONObject quizzes = root.getJSONObject("quizzes");
            JSONArray questions = quizzes.getJSONArray("questions");
            StringBuilder qb = new StringBuilder();
            qb.append(quizzes.optString("title")).append("\n");
            qb.append(quizzes.optString("instructions")).append("\n\n");

            // List each question + options
            for (int i = 0; i < questions.length(); i++) {
                JSONObject q = questions.getJSONObject(i);
                qb.append(i + 1).append(". ").append(q.optString("question")).append("\n");
                if (q.has("options")) {
                    JSONArray opts = q.getJSONArray("options");
                    char label = 'A';
                    for (int m = 0; m < opts.length(); m++) {
                        qb.append("   ").append(label++).append(") ").append(opts.getString(m)).append("\n");
                    }
                }
                qb.append("\n");
            }

            // Now append the answer key
            JSONObject answerKey = quizzes.getJSONObject("answer_key");
            qb.append("Answer Key:\n");
            for (int i = 1; i <= questions.length(); i++) {
                String qnum = String.valueOf(i);
                String ans = answerKey.optString(qnum, "");
                qb.append(qnum).append(": ").append(ans).append("\n");
            }

            quizContent = qb.toString().trim();

            // Display both
            notesView.setText(notesContent);
            quizView.setText(quizContent);

        } catch (Exception e) {
            Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveContent(String content, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Toast.makeText(this, filename + " saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}
