package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.auth.PasswordUtils;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.User;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword, etSecurityAnswer;
    private Spinner spinnerSecurityQuestion;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecurityQuestion.setAdapter(adapter);

        btnCreateAccount.setOnClickListener(v -> createAccount());
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String securityAnswer = etSecurityAnswer.getText().toString().trim();
        String securityQuestion = spinnerSecurityQuestion.getSelectedItem().toString();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (dbHelper.usernameExists(username)) {
            etUsername.setError("That username is already taken");
            etUsername.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(email)) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address, or leave it blank");
                etEmail.requestFocus();
                return;
            }
            if (dbHelper.emailExists(email)) {
                etEmail.setError("That email is already registered");
                etEmail.requestFocus();
                return;
            }
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(securityAnswer)) {
            etSecurityAnswer.setError("Please answer the security question");
            etSecurityAnswer.requestFocus();
            return;
        }

        String passwordSalt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hash(password, passwordSalt);

        String answerSalt = PasswordUtils.generateSalt();
        String answerHash = PasswordUtils.hash(securityAnswer.toLowerCase(), answerSalt);

        String recoveryCode = PasswordUtils.generateRecoveryCode();
        String recoverySalt = PasswordUtils.generateSalt();
        String recoveryHash = PasswordUtils.hash(PasswordUtils.normalizeCode(recoveryCode), recoverySalt);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt(passwordSalt);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswerHash(answerHash);
        user.setSecurityAnswerSalt(answerSalt);
        user.setRecoveryCodeHash(recoveryHash);
        user.setRecoveryCodeSalt(recoverySalt);

        long id = dbHelper.addUser(user);
        if (id > 0) {
            showRecoveryCodeDialog(recoveryCode);
        } else {
            Toast.makeText(this, "Could not create account. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRecoveryCodeDialog(String recoveryCode) {
        new AlertDialog.Builder(this)
                .setTitle("Save Your Recovery Code")
                .setMessage("Write this code down somewhere safe. If you forget your password and can't " +
                        "remember your security answer, this code is the only other way to reset it:\n\n" +
                        recoveryCode + "\n\nThis code will not be shown again.")
                .setCancelable(false)
                .setPositiveButton("I've Saved It", (dialog, which) -> {
                    Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                })
                .show();
    }
}