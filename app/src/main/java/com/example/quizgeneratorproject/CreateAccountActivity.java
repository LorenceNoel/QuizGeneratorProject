package com.example.quizgeneratorproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.auth.UserProfileChangeRequest;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "CreateAccountAct";

    private EditText fullnameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button createButton;
    private SignInButton googleSignInButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

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
                        Toast.makeText(this, "Google Sign-In failed: no data", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // View references
        fullnameInput         = findViewById(R.id.fullname_input);
        emailInput            = findViewById(R.id.email_input);
        passwordInput         = findViewById(R.id.password_input);
        confirmPasswordInput  = findViewById(R.id.confirm_password_input);
        createButton          = findViewById(R.id.create_button);
        googleSignInButton    = findViewById(R.id.google_sign_in_button);

        createButton.setOnClickListener(v -> createAccount());

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Force account chooser for Google, binding the callback to this Activity
        googleSignInButton.setOnClickListener(v -> {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, task -> {
                        // Now launched "from" your app's main thread
                        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
                    });
        });
    }

    private void createAccount() {
        String fullName = fullnameInput.getText().toString().trim();
        String email    = emailInput.getText().toString().trim();
        String pw       = passwordInput.getText().toString();
        String confirm  = confirmPasswordInput.getText().toString();

        // Input validation
        if (fullName.isEmpty()) {
            fullnameInput.setError("Enter full name");
            fullnameInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter valid email");
            emailInput.requestFocus();
            return;
        }
        if (pw.length() < 6) {
            passwordInput.setError("Password must be >=6 chars");
            passwordInput.requestFocus();
            return;
        }
        if (!pw.equals(confirm)) {
            confirmPasswordInput.setError("Passwords must match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Create user
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnSuccessListener(authRes -> {
                    FirebaseUser user = authRes.getUser();
                    if (user != null) {
                        Log.d(TAG, "createAccount succeeded: UID=" + user.getUid());
                        user.updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build())
                                .addOnSuccessListener(aVoid -> showAccountCreatedAndProceed())
                                .addOnFailureListener(e -> Log.e(TAG, "Profile update error", e));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createAccount failed", e);
                    Toast.makeText(this, "Sign-up failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void handleGoogleSignIn(Intent data) {
        try {
            GoogleSignInAccount acct = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);

            if (acct != null && acct.getIdToken() != null) {
                AuthCredential cred = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.signInWithCredential(cred)
                        .addOnSuccessListener(res -> showAccountCreatedAndProceed())
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Google auth failed", e);
                            Toast.makeText(this, "Authentication failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in exception, code: "
                    + e.getStatusCode() + ", msg: "
                    + e.getMessage(), e);
            Toast.makeText(this, "Google sign-in error: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show a non-cancelable dialog notifying account creation,
     * then navigate to MainActivity after a short delay.
     */
    private void showAccountCreatedAndProceed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Welcome!")
                .setMessage("Your account has been created. Please sign in using your new credentials.")
                .setCancelable(false)
                .create();
        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            navigateToMain();
        }, 3000);
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
