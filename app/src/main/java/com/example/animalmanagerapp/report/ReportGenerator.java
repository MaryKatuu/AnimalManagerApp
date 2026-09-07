package com.example.animalmanagerapp.report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;

import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds a printable/shareable PDF summarising the farm's livestock:
 * active animals grouped by type, head-count totals, sold/archive history
 * with sales revenue. Uses Android's built-in PdfDocument (no external
 * library needed).
 */
public class ReportGenerator {

    private static final int PAGE_WIDTH = 595;  // A4 at 72dpi
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;

    private final Context context;
    private final DatabaseHelper dbHelper;

    private PdfDocument pdfDocument;
    private PdfDocument.Page currentPage;
    private Canvas canvas;
    private int pageNumber;
    private float y;

    private Paint titlePaint, headingPaint, subheadingPaint, bodyPaint, mutedPaint, linePaint;

    public ReportGenerator(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(this.context);
        setupPaints();
    }

    private void setupPaints() {
        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFF1B5E20);
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);

        headingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headingPaint.setColor(0xFF1B5E20);
        headingPaint.setTextSize(14);
        headingPaint.setFakeBoldText(true);

        subheadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subheadingPaint.setColor(0xFF1A1A1A);
        subheadingPaint.setTextSize(12);
        subheadingPaint.setFakeBoldText(true);

        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(0xFF1A1A1A);
        bodyPaint.setTextSize(11);

        mutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mutedPaint.setColor(0xFF616161);
        mutedPaint.setTextSize(10);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFFDDDDDD);
        linePaint.setStrokeWidth(1);
    }

    public File generateFarmReport() throws IOException {
        pdfDocument = new PdfDocument();
        pageNumber = 0;
        currentPage = null;
        startNewPage();

        drawLine("AnimalManagerApp — Farm Report", titlePaint, 14);
        drawLine("Generated on " + DateUtils.toDisplayFormat(DateUtils.todayIso()), mutedPaint, 16);

        drawSectionDivider();
        drawLine("Livestock Totals by Type", headingPaint, 10);
        Map<String, Integer> headCounts = dbHelper.getHeadCountByType();
        if (headCounts.isEmpty()) {
            drawBody("No active livestock currently recorded.");
        } else {
            for (Map.Entry<String, Integer> entry : headCounts.entrySet()) {
                drawBody("  " + entry.getKey() + ":  " + entry.getValue());
            }
            drawBody("Total head count across all types: " + dbHelper.getTotalHeadCount());
        }

        drawSectionDivider();
        drawLine("Active Animals — Full Detail", headingPaint, 10);
        List<Animal> activeAnimals = dbHelper.getAllAnimals(null);
        if (activeAnimals.isEmpty()) {
            drawBody("No active animals currently recorded.");
        } else {
            for (Animal animal : activeAnimals) {
                drawAnimalBlock(animal, false);
            }
        }

        drawSectionDivider();
        drawLine("Sold Animals (Archive)", headingPaint, 10);
        List<Animal> soldAnimals = dbHelper.getSoldAnimals(null);
        if (soldAnimals.isEmpty()) {
            drawBody("No animals sold yet.");
        } else {
            for (Animal animal : soldAnimals) {
                drawAnimalBlock(animal, true);
            }
            drawLine("Total sales revenue: KES " + String.format("%.2f", dbHelper.getTotalSalesRevenue()),
                    subheadingPaint, 6);
        }

        pdfDocument.finishPage(currentPage);

        File reportsDir = new File(context.getCacheDir(), "reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();
        File file = new File(reportsDir, "Livestock_Report_" + DateUtils.todayIso() + ".pdf");
        try (FileOutputStream out = new FileOutputStream(file)) {
            pdfDocument.writeTo(out);
        } finally {
            pdfDocument.close();
        }
        return file;
    }

    private void drawAnimalBlock(Animal animal, boolean isSold) {
        ensureSpace(80);
        String name = "Tag #" + animal.getTagNumber() + " — " + animal.getAnimalType();
        if (!TextUtils.isEmpty(animal.getVariety())) name += " (" + animal.getVariety() + ")";
        drawLine(name, subheadingPaint, 4);

        drawBody("Quantity: " + (TextUtils.isEmpty(animal.getQuantity()) ? "1" : animal.getQuantity()) +
                "   Sex: " + safe(animal.getSex()) + "   Age: " + safe(animal.getAge()));

        if (isSold) {
            drawBody("Sold: " + DateUtils.toDisplayFormat(animal.getSoldDate()) +
                    "   Sale amount: " + (TextUtils.isEmpty(animal.getSaleAmount()) ? "Not recorded" : "KES " + animal.getSaleAmount()));
        } else {
            drawBody("Acquired: " + DateUtils.toDisplayFormat(animal.getDateAcquired()));
        }

        if (!TextUtils.isEmpty(animal.getLayingCount()) || !TextUtils.isEmpty(animal.getTraysCollected())) {
            drawBody("Currently laying: " + safe(animal.getLayingCount()) +
                    "   Trays collected: " + safe(animal.getTraysCollected()));
        }

        List<HealthEvent> events = dbHelper.getEventsForAnimal(animal.getId());
        if (!events.isEmpty()) {
            drawLine(events.size() + " health/feeding event" + (events.size() == 1 ? "" : "s") + " recorded.",
                    mutedPaint, 10);
        } else {
            y += 8;
        }
    }

    // ---------------- drawing helpers ----------------

    private void startNewPage() {
        if (currentPage != null) {
            pdfDocument.finishPage(currentPage);
        }
        pageNumber++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        currentPage = pdfDocument.startPage(pageInfo);
        canvas = currentPage.getCanvas();
        y = MARGIN;
    }

    private void ensureSpace(float needed) {
        if (y + needed > PAGE_HEIGHT - MARGIN) {
            startNewPage();
        }
    }

    private void drawLine(String text, Paint paint, float extraSpacing) {
        ensureSpace(paint.getTextSize() + extraSpacing);
        canvas.drawText(text, MARGIN, y + paint.getTextSize(), paint);
        y += paint.getTextSize() + extraSpacing;
    }

    private void drawBody(String text) {
        for (String line : wrapText(text, bodyPaint, PAGE_WIDTH - 2f * MARGIN)) {
            drawLine(line, bodyPaint, 4);
        }
    }

    private void drawSectionDivider() {
        ensureSpace(20);
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 16;
    }

    private List<String> wrapText(String text, Paint paint, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = (current.length() == 0) ? word : current + " " + word;
            if (paint.measureText(candidate) > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private String safe(String s) {
        return TextUtils.isEmpty(s) ? "Not recorded" : s;
    }
}