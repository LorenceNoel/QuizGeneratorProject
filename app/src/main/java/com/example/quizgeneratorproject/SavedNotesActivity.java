package com.example.quizgeneratorproject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SavedNotesActivity extends AppCompatActivity {

    private LinearLayout notesContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        notesContainer = findViewById(R.id.notes_container);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchAndDisplayNotes();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_saved_notes);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, ProfileSettingsActivity.class));
                return true;
            } else if (id == R.id.nav_saved_notes) {
                // already here
                return true;
            } else if (id == R.id.nav_saved_quizzes) {
                startActivity(new Intent(this, SavedQuizzesActivity.class));
                return true;
            }
            return false;
        });
    }

    private void addDateHeader(String dateString) {
        TextView header = new TextView(this);
        header.setText(dateString);
        header.setTextSize(18);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(getResources().getColor(R.color.white));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 32, 0, 16);
        header.setLayoutParams(params);

        notesContainer.addView(header);
    }


    private void fetchAndDisplayNotes() {
        notesContainer.removeAllViews();

        CollectionReference colRef = db
                .collection("users")
                .document(currentUser.getUid())
                .collection("notes");

        colRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "No notes found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1) Group by date
                    Map<String, List<String>> grouped = new LinkedHashMap<>();
                    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String content = doc.getString("content");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        String dateKey = ts != null
                                ? fmt.format(ts.toDate())
                                : "Unknown date";

                        if (!grouped.containsKey(dateKey)) {
                            grouped.put(dateKey, new ArrayList<>());
                        }
                        grouped.get(dateKey).add(content);
                    }

                    // 2) Render: for each date, add header + its notes
                    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                        addDateHeader(entry.getKey());
                        for (String note : entry.getValue()) {
                            addNoteCard(note);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
                });
    }


    private void addNoteCard(String note) {
        // inflate our new card layout
        View noteView = LayoutInflater.from(this)
                .inflate(R.layout.note_card, notesContainer, false);

        TextView previewText = noteView.findViewById(R.id.preview_text);
        TextView fullText = noteView.findViewById(R.id.full_text);
        TextView toggleText = noteView.findViewById(R.id.toggle_text);

        // set preview to first 100 chars (or full note if shorter)
        String preview = note.length() > 100
                ? note.substring(0, 100) + "..."
                : note;
        previewText.setText(preview);
        fullText.setText(note);

        toggleText.setOnClickListener(v -> {
            if (fullText.getVisibility() == View.GONE) {
                // expand
                fullText.setVisibility(View.VISIBLE);
                previewText.setVisibility(View.GONE);
                toggleText.setText("Show less");
            } else {
                // collapse
                fullText.setVisibility(View.GONE);
                previewText.setVisibility(View.VISIBLE);
                toggleText.setText("Show more");
            }
        });

        notesContainer.addView(noteView);
    }
}
