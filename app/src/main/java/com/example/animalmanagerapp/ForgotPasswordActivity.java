package com.example.animalmanagerapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.auth.PasswordUtils;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.User;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private EditText etIdentifier, etSecurityAnswer, etRecoveryCode, etNewPassword, etConfirmNewPassword;
    private RadioGroup rgResetMethod;
    private TextView tvSecurityQuestionDisplay;
    private Button btnFindAccount, btnVerify, btnResetPassword;

    private DatabaseHelper dbHelper;
    private User currentUser;
    private boolean verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        etIdentifier = findViewById(R.id.etIdentifier);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        etRecoveryCode = findViewById(R.id.etRecoveryCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        rgResetMethod = findViewById(R.id.rgResetMethod);
        tvSecurityQuestionDisplay = findViewById(R.id.tvSecurityQuestionDisplay);

        btnFindAccount = findViewById(R.id.btnFindAccount);
        btnVerify = findViewById(R.id.btnVerify);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnFindAccount.setOnClickListener(v -> findAccount());

        rgResetMethod.setOnCheckedChangeListener((group, checkedId) -> {
            boolean useSecurityQuestion = checkedId == R.id.rbSecurityQuestion;
            etSecurityAnswer.setVisibility(useSecurityQuestion ? View.VISIBLE : View.GONE);
            etRecoveryCode.setVisibility(useSecurityQuestion ? View.GONE : View.VISIBLE);
        });

        btnVerify.setOnClickListener(v -> verifyIdentity());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void findAccount() {
        String identifier = etIdentifier.getText().toString().trim();
        if (TextUtils.isEmpty(identifier)) {
            etIdentifier.setError("Enter your username or email");
            etIdentifier.requestFocus();
            return;
        }

        currentUser = dbHelper.getUserByIdentifier(identifier);
        if (currentUser == null) {
            Toast.makeText(this, "No account found with that username or email", Toast.LENGTH_SHORT).show();
            return;
        }

        tvSecurityQuestionDisplay.setText(currentUser.getSecurityQuestion());
        layoutStep2.setVisibility(View.VISIBLE);
        btnFindAccount.setEnabled(false);
        etIdentifier.setEnabled(false);
    }

    private void verifyIdentity() {
        if (currentUser == null) return;

        boolean useSecurityQuestion = rgResetMethod.getCheckedRadioButtonId() == R.id.rbSecurityQuestion;
        boolean matches;

        if (useSecurityQuestion) {
            String answer = etSecurityAnswer.getText().toString().trim().toLowerCase();
            if (TextUtils.isEmpty(answer)) {
                etSecurityAnswer.setError("Please enter your answer");
                etSecurityAnswer.requestFocus();
                return;
            }
            matches = PasswordUtils.verify(answer, currentUser.getSecurityAnswerSalt(), currentUser.getSecurityAnswerHash());
        } else {
            String code = etRecoveryCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                etRecoveryCode.setError("Please enter your recovery code");
                etRecoveryCode.requestFocus();
                return;
            }
            String normalized = PasswordUtils.normalizeCode(code);
            matches = PasswordUtils.verify(normalized, currentUser.getRecoveryCodeSalt(), currentUser.getRecoveryCodeHash());
        }

        if (!matches) {
            Toast.makeText(this, "That doesn't match our records. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        verified = true;
        layoutStep3.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        Toast.makeText(this, "Verified! Set your new password below.", Toast.LENGTH_SHORT).show();
    }

    private void resetPassword() {
        if (!verified || currentUser == null) {
            Toast.makeText(this, "Please verify your identity first", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmNewPassword.getText().toString();

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        String newSalt = PasswordUtils.generateSalt();
        String newHash = PasswordUtils.hash(newPassword, newSalt);
        dbHelper.updatePassword(currentUser.getId(), newHash, newSalt);

        Toast.makeText(this, "Password reset successfully. Please log in.", Toast.LENGTH_LONG).show();
        finish();
    }
}