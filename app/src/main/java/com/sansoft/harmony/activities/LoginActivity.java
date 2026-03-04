package com.sansoft.harmony.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sansoft.harmony.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signupTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signupTextView = findViewById(R.id.signup_text_view);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all the fields",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login successful",
                                Toast.LENGTH_SHORT).show();
                            // TODO: Navigate to the main activity
                            finish();
                        } else {
                            final String message = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Login failed: " + message,
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signupTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
