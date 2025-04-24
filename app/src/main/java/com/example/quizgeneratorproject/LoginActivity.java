package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText emailInput, passwordInput;
    private Button loginButton, createAccountButton;
    private TextView forgotPassword;
    private SignInButton googleSignInButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        handleGoogleSignIn(data);
                    } else {
                        Log.d(TAG, "Google Sign-In returned no data");
                    }
                }
        );

        // View references
        emailInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        forgotPassword = findViewById(R.id.forgot_password);
        loginButton = findViewById(R.id.login_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        createAccountButton = findViewById(R.id.create_account_button);

        // Email/password login
        loginButton.setOnClickListener(v -> signInWithEmail());

        // Forgot password
        forgotPassword.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter a valid email");
                emailInput.requestFocus();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Password reset failed", e);
                        Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        googleSignInButton.setColorScheme(SignInButton.COLOR_DARK);
        googleSignInButton.setOnClickListener(v -> {
            // Force account chooser by signing out first
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
            });
        });

        // Create account navigation
        createAccountButton.setOnClickListener(v -> startActivity(
                new Intent(LoginActivity.this, CreateAccountActivity.class)
        ));
    }

    private void signInWithEmail() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Enter your password");
            passwordInput.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "Email login success: " + user.getEmail());
                        navigateToMain();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Email login failed", e);
                    Toast.makeText(this, "Login failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void handleGoogleSignIn(Intent data) {
        try {
            GoogleSignInAccount acct = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);
            if (acct != null && acct.getIdToken() != null) {
                AuthCredential cred = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.signInWithCredential(cred)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Google login success: " + user.getEmail());
                                navigateToMain();
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Google auth failed", e));
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in exception", e);
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
