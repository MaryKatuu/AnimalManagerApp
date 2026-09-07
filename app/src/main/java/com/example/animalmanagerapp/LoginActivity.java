package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.auth.PasswordUtils;
import com.example.animalmanagerapp.auth.SessionManager;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etIdentifier, etPassword;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            goToDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        tvGoToSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }

    private void attemptLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(identifier)) {
            etIdentifier.setError("Enter your username or email");
            etIdentifier.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        User user = dbHelper.getUserByIdentifier(identifier);
        if (user == null) {
            Toast.makeText(this, "No account found with that username or email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PasswordUtils.verify(password, user.getPasswordSalt(), user.getPasswordHash())) {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.login(user.getId(), user.getUsername());
        goToDashboard();
    }

    private void goToDashboard() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}