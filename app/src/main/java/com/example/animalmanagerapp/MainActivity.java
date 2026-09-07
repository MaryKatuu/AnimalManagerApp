package com.example.animalmanagerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.adapter.AnimalAdapter;
import com.example.animalmanagerapp.auth.SessionManager;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.report.PdfPrintDocumentAdapter;
import com.example.animalmanagerapp.report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Dashboard / home screen: shows quick totals and animals that haven't had
 * a health/feeding event logged recently, plus entry points to add or
 * browse animals, view the sold-animal archive, generate a PDF report,
 * and log out.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TextView tvTotalAnimals, tvAllGood;
    private RecyclerView rvNeedsAttention;
    private AnimalAdapter adapter;

    private static final int OVERDUE_DAYS_THRESHOLD = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvTotalAnimals = findViewById(R.id.tvTotalAnimals);
        tvAllGood = findViewById(R.id.tvAllGood);
        rvNeedsAttention = findViewById(R.id.rvNeedsAttention);

        Button btnAddAnimal = findViewById(R.id.btnAddAnimal);
        Button btnViewAnimals = findViewById(R.id.btnViewAnimals);
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        rvNeedsAttention.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(new java.util.ArrayList<>(), dbHelper, animal -> {
            Intent intent = new Intent(MainActivity.this, AnimalDetailsActivity.class);
            intent.putExtra("animal_id", animal.getId());
            startActivity(intent);
        });
        rvNeedsAttention.setAdapter(adapter);

        btnAddAnimal.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddAnimalActivity.class)));

        btnViewAnimals.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));

        btnMenu.setOnClickListener(this::showDashboardMenu);
    }

    private void showDashboardMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.dashboard_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_all_animals) {
                startActivity(new Intent(MainActivity.this, AnimalListActivity.class));
                return true;
            } else if (id == R.id.menu_archive) {
                startActivity(new Intent(MainActivity.this, ArchiveActivity.class));
                return true;
            } else if (id == R.id.menu_generate_report) {
                generateAndOfferReport();
                return true;
            } else if (id == R.id.menu_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
                ReportGenerator generator = new ReportGenerator(MainActivity.this);
                File reportFile = generator.generateFarmReport();
                runOnUiThread(() -> offerReportActions(reportFile));
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
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

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            loadSummary();
        }
    }

    private void loadSummary() {
        int totalAnimals = dbHelper.getTotalAnimalCount();
        tvTotalAnimals.setText(String.valueOf(totalAnimals));

        List<Animal> needsAttention = dbHelper.getAnimalsNeedingAttention(OVERDUE_DAYS_THRESHOLD);
        adapter.updateData(needsAttention);

        if (needsAttention.isEmpty()) {
            tvAllGood.setVisibility(View.VISIBLE);
            rvNeedsAttention.setVisibility(View.GONE);
        } else {
            tvAllGood.setVisibility(View.GONE);
            rvNeedsAttention.setVisibility(View.VISIBLE);
        }
    }
}