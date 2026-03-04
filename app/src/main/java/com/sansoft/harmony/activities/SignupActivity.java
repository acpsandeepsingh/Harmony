package com.sansoft.harmony.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.sansoft.harmony.R;

public class SignupActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;
    private TextView loginTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        loginTextView = findViewById(R.id.login_text_view);

        firebaseAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(v -> {
            final String name = nameEditText.getText().toString().trim();
            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();
            final String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all the fields",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            final UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            task.getResult().getUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this,
                                                "Signup successful",
                                                Toast.LENGTH_SHORT).show();
                                            // TODO: Navigate to the main activity
                                            finish();
                                        } else {
                                            final String message =
                                                profileTask.getException().getMessage();
                                            Toast.makeText(SignupActivity.this,
                                                "Profile update failed: " + message,
                                                Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            final String message = task.getException().getMessage();
                            Toast.makeText(SignupActivity.this, "Signup failed: " + message,
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }
}
