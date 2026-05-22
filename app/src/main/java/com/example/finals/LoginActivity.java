package com.example.finals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // ✅ ADMIN CREDENTIALS
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";

    // Google Sign-In Variables
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    // Views
    android.widget.EditText etEmail, etPassword;
    android.widget.Button btnLogin, google_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        google_btn = findViewById(R.id.google_btn);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // GOOGLE SIGN-IN CONFIGURATION
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("841177066994-iqlbaee9imigdnn3jherkgpdb6ejt9v6.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Clear active session on launch for testing safety
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }

        btnLogin.setOnClickListener(v -> {
            String inputUser = etEmail.getText().toString().trim();
            String inputPass = etPassword.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill up all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ CHECK KUNG ADMIN
            if (inputUser.equals(ADMIN_USERNAME) && inputPass.equals(ADMIN_PASSWORD)) {
                // Sign out any old normal users so data doesn't get cross-mixed
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                }

                Toast.makeText(LoginActivity.this, "✅ Welcome Admin!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, AdminDashboard.class);

                // This clears the activity stack cleanly
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // ✅ USER: RUNS STANDARD FIREBASE AUTH LOGINS
                loginNormalUser(inputUser, inputPass);
            }
        });

        // GOOGLE SIGN-IN BUTTON CLICK
        google_btn.setOnClickListener(v -> signInWithGoogle());
    }

    // LOGIC FOR NORMAL TENANT USERS
    private void loginNormalUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                            intent.putExtra("username", user.getEmail());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "❌ Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // GOOGLE SIGN-IN FUNCTIONS
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account.getEmail());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                            intent.putExtra("username", email);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}