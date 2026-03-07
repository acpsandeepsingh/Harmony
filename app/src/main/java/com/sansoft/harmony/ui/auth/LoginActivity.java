package com.sansoft.harmony.ui.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TextView textView = new TextView(this);
        textView.setText("Login with Firebase Authentication");
        setContentView(textView);
    }
}
