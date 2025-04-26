package com.example.quizgeneratorproject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Displays quizzes saved by the current user, grouped by save-date.
 * Each quiz shows a three-line preview and can be expanded/collapsed.
 */
public class SavedQuizzesActivity extends AppCompatActivity {

    // Container for dynamically added date headers and quiz cards
    private LinearLayout quizzesList;
    // Firestore entry point
    private FirebaseFirestore db;
    // Currently authenticated user
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_quizzes_activity);

        // Initialize Firestore and UI references
        quizzesList = findViewById(R.id.quizzes_list);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // No user? Bail out early with a toast
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kick off the Firestore fetch/group/render process
        fetchAndDisplayQuizzes();

        // Bottom nav setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Highlight the current tab
        bottomNav.setSelectedItemId(R.id.nav_saved_quizzes);
        bottomNav.setOnItemSelectedListener(item -> {
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
                // Already on this screen
                return true;
            }
            return false;
        });
    }

    /**
     * Fetches the user's saved quizzes from Firestore, groups them by
     * date, and renders the UI: a date header followed by expandable cards.
     */
    private void fetchAndDisplayQuizzes() {
        // Clear previous views (useful if reloading)
        quizzesList.removeAllViews();

        // Reference to users/{uid}/quizzes collection
        CollectionReference colRef = db
                .collection("users")
                .document(currentUser.getUid())
                .collection("quizzes");

        // Query most recent first
        colRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot snaps) -> {
                    if (snaps.isEmpty()) {
                        Toast.makeText(this, "No quizzes found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Use LinkedHashMap to preserve insertion order of dates
                    Map<String, List<String>> grouped = new LinkedHashMap<>();
                    // Format like "Apr 26, 2025"
                    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                    // Group each doc's content by its formatted date string
                    for (QueryDocumentSnapshot doc : snaps) {
                        String content = doc.getString("content");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        String dateKey = (ts != null)
                                ? fmt.format(ts.toDate())
                                : "Unknown date";

                        // computeIfAbsent simplifies the list creation
                        grouped.computeIfAbsent(dateKey, k -> new ArrayList<>())
                                .add(content);
                    }

                    // Render headers and quiz cards in order
                    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                        addDateHeader(entry.getKey());
                        for (String quiz : entry.getValue()) {
                            addQuizCard(quiz);
                        }
                    }
                })
                .addOnFailureListener((@NonNull Exception e) ->
                        Toast.makeText(this, "Failed to load quizzes.", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Inserts a bold date header into the LinearLayout.
     *
     * @param dateString formatted date like "Apr 26, 2025"
     */
    private void addDateHeader(String dateString) {
        TextView header = new TextView(this);
        header.setText(dateString);
        header.setTextSize(18);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(getResources().getColor(R.color.white));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(0, 32, 0, 16);
        header.setLayoutParams(p);

        quizzesList.addView(header);
    }

    /**
     * Inflates a quiz_card layout, populates preview/full text,
     * and sets up the expand/collapse toggle behavior.
     *
     * @param quizContent raw quiz data (e.g. JSON or text)
     */
    private void addQuizCard(String quizContent) {
        // Inflate the reusable quiz_card.xml
        View quizView = LayoutInflater.from(this)
                .inflate(R.layout.quiz_card, quizzesList, false);

        TextView preview = quizView.findViewById(R.id.preview_text);
        TextView full = quizView.findViewById(R.id.full_text);
        TextView toggle = quizView.findViewById(R.id.toggle_text);

        // Show only first 100 chars (or the full string if shorter)
        String shortText = quizContent.length() > 100
                ? quizContent.substring(0, 100) + "..."
                : quizContent;
        preview.setText(shortText);
        full.setText(quizContent);

        // Toggle between expanded/collapsed states
        toggle.setOnClickListener(v -> {
            if (full.getVisibility() == View.GONE) {
                full.setVisibility(View.VISIBLE);
                preview.setVisibility(View.GONE);
                toggle.setText("Show less");
            } else {
                full.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                toggle.setText("Show more");
            }
        });

        quizzesList.addView(quizView);
    }
}
