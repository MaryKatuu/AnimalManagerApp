package com.example.animalmanagerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.animalmanagerapp.auth.SessionManager;
import com.example.animalmanagerapp.report.PdfPrintDocumentAdapter;
import com.example.animalmanagerapp.report.ReportGenerator;

import java.io.File;
import java.io.IOException;

/** Settings screen: report generation and logout, reached via the hamburger icon. */
public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        LinearLayout rowGenerateReport = findViewById(R.id.rowGenerateReport);
        LinearLayout rowLogout = findViewById(R.id.rowLogout);

        rowGenerateReport.setOnClickListener(v -> generateAndOfferReport());
        rowLogout.setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateAndOfferReport() {
        Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                ReportGenerator generator = new ReportGenerator(SettingsActivity.this);
                File reportFile = generator.generateFarmReport();
                runOnUiThread(() -> offerReportActions(reportFile));
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this,
                        "Could not generate report. Please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void offerReportActions(File reportFile) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", reportFile);
        new AlertDialog.Builder(this)
                .setTitle("Report Ready")
                .setMessage("Your livestock report has been generated. What would you like to do?")
                .setPositiveButton("Share", (dialog, which) -> shareReport(uri))
                .setNeutralButton("Print", (dialog, which) -> printReport(reportFile))
                .setNegativeButton("Close", null)
                .show();
    }

    private void shareReport(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Livestock Report"));
    }

    private void printReport(File reportFile) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            printManager.print("Livestock_Report", new PdfPrintDocumentAdapter(reportFile, "Livestock_Report"), null);
        }
    }
}