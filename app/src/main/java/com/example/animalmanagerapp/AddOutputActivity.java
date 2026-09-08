package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.OutputLog;

import java.util.Calendar;

/** Logs a single production output entry (eggs, milk, etc.) for an animal. */
public class AddOutputActivity extends AppCompatActivity {

    private EditText etQuantity, etNotes;
    private Spinner spinnerUnit;
    private Button btnOutputDate, btnSaveOutput;
    private TextView tvFormTitle;

    private String outputDateIso = null;
    private DatabaseHelper dbHelper;
    private long animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_output);

        dbHelper = new DatabaseHelper(this);
        animalId = getIntent().getLongExtra("animal_id", -1);

        etQuantity = findViewById(R.id.etQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        btnOutputDate = findViewById(R.id.btnOutputDate);
        etNotes = findViewById(R.id.etNotes);
        btnSaveOutput = findViewById(R.id.btnSaveOutput);
        tvFormTitle = findViewById(R.id.tvFormTitle);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.output_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);

        Animal animal = dbHelper.getAnimal(animalId);
        if (animal != null) {
            tvFormTitle.setText("Log Output — " + animal.getTagNumber());
        }

        btnOutputDate.setOnClickListener(v -> showDatePicker());
        btnSaveOutput.setOnClickListener(v -> saveOutput());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    outputDateIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    btnOutputDate.setText(DateUtils.toDisplayFormat(outputDateIso));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveOutput() {
        if (animalId == -1) {
            Toast.makeText(this, "Missing animal reference. Please go back and try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String quantity = etQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(quantity) || parseOrZero(quantity) <= 0) {
            etQuantity.setError("Enter a quantity greater than 0");
            etQuantity.requestFocus();
            return;
        }
        if (outputDateIso == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        OutputLog log = new OutputLog();
        log.setAnimalId(animalId);
        log.setOutputDate(outputDateIso);
        log.setQuantity(quantity);
        log.setUnit(spinnerUnit.getSelectedItem().toString());
        log.setNotes(etNotes.getText().toString().trim());

        long id = dbHelper.addOutputLog(log);
        if (id > 0) {
            Toast.makeText(this, "Output logged", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save output. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private double parseOrZero(String text) {
        try { return Double.parseDouble(text.trim()); } catch (NumberFormatException e) { return 0; }
    }
}