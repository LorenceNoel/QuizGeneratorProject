package com.example.quizgeneratorproject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;
import com.google.firebase.vertexai.type.GenerationConfig;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiClient {
    private static final String TAG = "GeminiClient";

    // System prompt defining role and scope
    private static final String SYSTEM_PROMPT = "You are an Academic Tutor specializing in high school and college courses. " + "I will give you keywords and topics to generate notes and a quiz. " + "Separate output into 'notes' and 'quizzes' sections with the exact JSON structure: {\"notes\":{...},\"quizzes\":{...}}. " + "Respond only with valid JSON and say 'I am limited to generating notes and quizzes only' if asked outside scope.";

    // Formatting instructions and example skeleton
    private static final String FORMAT_PROMPT = "Set quiz questions in random order (easy to hard mixed). Notes must have 3 sections and a conclusion; quiz must have 10 questions.\n" + "Example JSON skeleton:\n" + "{\n" + "  \"notes\": {\n" + "    \"title\": \"Notes on [Topic]\",\n" + "    \"introduction\": \"Brief intro...\",\n" + "    \"section_1\": {\n" + "      \"title\": \"[Keyword1]\",\n" + "      \"content\": \"Explanation...\",\n" + "      \"subtopics\": [{ \"title\": \"...\", \"content\": \"...\" }],\n" + "      \"examples\": [\"...\"]\n" + "    },\n" + "    \"section_2\": { ... },\n" + "    \"section_3\": { ... },\n" + "    \"conclusion\": \"...\"\n" + "  },\n" + "  \"quizzes\": {\n" + "    \"questions\": [ /* 10 questions */ ],\n" + "    \"answer_key\": { /* keys */ }\n" + "  }\n" + "}";

    private final GenerativeModelFutures model;
    private final Executor executor;
    private final Handler mainHandler;

    /**
     * Initialize Firebase, configure the model to return JSON.
     */
    public GeminiClient(Context context) {
        FirebaseApp.initializeApp(context);

        // Build generation config with JSON response
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = FirebaseVertexAI.getInstance().generativeModel("gemini-2.0-flash", generationConfig);
        this.model = GenerativeModelFutures.from(gm);

        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Generate notes and quiz JSON for the given userInput.
     */
    public void generate(String userInput, Callback callback) {
        String prompt = SYSTEM_PROMPT + "\n\n" + FORMAT_PROMPT + "\n\nUser Input: " + userInput;

        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> future = model.generateContent(content);
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String json = result.getText();
                Log.d(TAG, "Generated JSON: " + json);
                mainHandler.post(() -> callback.onSuccess(json));
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Generation failed", t);
                mainHandler.post(() -> callback.onFailure(t));
            }
        }, executor);
    }

    /**
     * Callback interface for JSON results or errors.
     */
    public interface Callback {
        void onSuccess(String jsonResult);

        void onFailure(Throwable t);
    }
}
