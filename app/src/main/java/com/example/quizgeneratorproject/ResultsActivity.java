package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {
    private TextView notesView, quizView;
    private Button saveNotesBtn, saveQuizBtn;
    private String notesContent, quizContent;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

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

        // ---- Initialize Firebase ----
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in to save.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ---- Bind views ----
        notesView = findViewById(R.id.notes_view);
        quizView = findViewById(R.id.quiz_view);
        saveNotesBtn = findViewById(R.id.save_notes_button);
        saveQuizBtn = findViewById(R.id.save_quiz_button);

        // ---- UI styling ----
        int black = ContextCompat.getColor(this, R.color.black);
        notesView.setTextColor(black);
        quizView.setTextColor(black);

        notesView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card));
        quizView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card));
        notesView.setPadding(32, 32, 32, 32);
        quizView.setPadding(32, 32, 32, 32);

        saveNotesBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_primary));
        saveQuizBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_primary));
        saveNotesBtn.setTextColor(black);
        saveQuizBtn.setTextColor(black);
        saveNotesBtn.setAllCaps(false);
        saveQuizBtn.setAllCaps(false);

        // ---- Parse incoming JSON ----
        String json = getIntent().getStringExtra("RESULT_JSON");
        parseAndDisplay(json);

        // ---- Save listeners, now passing in the button itself ----
        saveNotesBtn.setOnClickListener(v ->
                saveToFirestore(notesContent, "notes", saveNotesBtn)
        );
        saveQuizBtn.setOnClickListener(v ->
                saveToFirestore(quizContent, "quizzes", saveQuizBtn)
        );
    }

    /**
     * Saves `content` into users/{uid}/{collectionName}, then on success:
     * - changes the button text to "Saved"
     * - disables the button
     */
    private void saveToFirestore(String content, String collectionName, Button btn) {
        CollectionReference colRef = db
                .collection("users")
                .document(currentUser.getUid())
                .collection(collectionName);

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("timestamp", FieldValue.serverTimestamp());

        colRef.add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // update UI: mark saved & disable
                        btn.setText("Saved");
                        btn.setEnabled(false);
                        Toast.makeText(ResultsActivity.this,
                                collectionName + " saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ResultsActivity.this,
                                "Failed to save " + collectionName, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void parseAndDisplay(String json) {
        try {
            JSONObject root = new JSONObject(json);

            // === Notes Section ===
            JSONObject notes = root.getJSONObject("notes");
            StringBuilder nb = new StringBuilder();
            nb.append(notes.optString("title")).append("\n\n");
            nb.append(notes.optString("introduction")).append("\n\n");
            for (int i = 1; i <= 3; i++) {
                String key = "section_" + i;
                if (!notes.has(key)) continue;
                JSONObject sec = notes.getJSONObject(key);
                nb.append(sec.optString("title"))
                        .append("\n")
                        .append(sec.optString("content"))
                        .append("\n");
                if (sec.has("subtopics")) {
                    JSONArray subs = sec.getJSONArray("subtopics");
                    if (subs.length() > 0) {
                        JSONObject sub = subs.getJSONObject(0);
                        nb.append("• ")
                                .append(sub.optString("title"))
                                .append(": ")
                                .append(sub.optString("content"))
                                .append("\n");
                    }
                }
                if (sec.has("examples")) {
                    JSONArray exs = sec.getJSONArray("examples");
                    if (exs.length() > 0) {
                        nb.append("- Example: ")
                                .append(exs.getString(0))
                                .append("\n");
                    }
                }
                nb.append("\n");
            }
            nb.append(notes.optString("conclusion")).append("\n");
            notesContent = nb.toString().trim();

            // === Quiz Section ===
            JSONObject quizzes = root.getJSONObject("quizzes");
            JSONArray questions = quizzes.getJSONArray("questions");
            StringBuilder qb = new StringBuilder();
            qb.append(quizzes.optString("title")).append("\n");
            qb.append(quizzes.optString("instructions")).append("\n\n");

            for (int i = 0; i < questions.length(); i++) {
                JSONObject q = questions.getJSONObject(i);
                qb.append(i + 1).append(". ").append(q.optString("question")).append("\n");
                if (q.has("options")) {
                    JSONArray opts = q.getJSONArray("options");
                    char label = 'A';
                    for (int m = 0; m < opts.length(); m++) {
                        qb.append("   ")
                                .append(label++)
                                .append(") ")
                                .append(opts.getString(m))
                                .append("\n");
                    }
                }
                qb.append("\n");
            }

            JSONObject answerKey = quizzes.getJSONObject("answer_key");
            qb.append("Answer Key:\n");
            for (int i = 1; i <= questions.length(); i++) {
                String qnum = String.valueOf(i);
                qb.append(qnum)
                        .append(": ")
                        .append(answerKey.optString(qnum, ""))
                        .append("\n");
            }

            quizContent = qb.toString().trim();

            notesView.setText(notesContent);
            quizView.setText(quizContent);

        } catch (Exception e) {
            Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
        }
    }
}
