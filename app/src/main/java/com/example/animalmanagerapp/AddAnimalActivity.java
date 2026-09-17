package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.animalmanagerapp.catalog.AnimalCatalog;
import com.example.animalmanagerapp.catalog.AnimalImageResolver;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.db.ValidationUtils;
import com.example.animalmanagerapp.model.Animal;

import java.util.Calendar;

public class AddAnimalActivity extends AppCompatActivity {

    private static final int REQUEST_BROWSE_ANIMALS = 200;

    private CardView cardSelectType;
    private ImageView ivSelectedTypeImage;
    private TextView tvSelectedTypeName;
    private Spinner spinnerSex;
    private EditText etVariety, etQuantity, etLayingCount, etTraysCollected, etTagNumber, etAge;
    private LinearLayout llPoultryFields;
    private Button btnDateAcquired, btnSaveAnimal;

    private String selectedAnimalType = null;
    private String dateAcquiredIso = null;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_animal);

        dbHelper = new DatabaseHelper(this);

        cardSelectType = findViewById(R.id.cardSelectType);
        ivSelectedTypeImage = findViewById(R.id.ivSelectedTypeImage);
        tvSelectedTypeName = findViewById(R.id.tvSelectedTypeName);
        spinnerSex = findViewById(R.id.spinnerSex);
        etVariety = findViewById(R.id.etVariety);
        etQuantity = findViewById(R.id.etQuantity);
        etLayingCount = findViewById(R.id.etLayingCount);
        etTraysCollected = findViewById(R.id.etTraysCollected);
        etTagNumber = findViewById(R.id.etTagNumber);
        etAge = findViewById(R.id.etAge);
        llPoultryFields = findViewById(R.id.llPoultryFields);
        btnDateAcquired = findViewById(R.id.btnDateAcquired);
        btnSaveAnimal = findViewById(R.id.btnSaveAnimal);

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);

        cardSelectType.setOnClickListener(v ->
                startActivityForResult(new Intent(AddAnimalActivity.this, BrowseAnimalsActivity.class), REQUEST_BROWSE_ANIMALS));

        btnDateAcquired.setOnClickListener(v -> showDatePicker());
        btnSaveAnimal.setOnClickListener(v -> saveAnimal());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BROWSE_ANIMALS && resultCode == RESULT_OK && data != null) {
            selectedAnimalType = data.getStringExtra(BrowseAnimalsActivity.EXTRA_TYPE_NAME);
            tvSelectedTypeName.setText(selectedAnimalType);
            tvSelectedTypeName.setTextColor(getResources().getColor(R.color.text_primary));
            String overrideImage = dbHelper.getAnimalTypeImage(selectedAnimalType);
            AnimalImageResolver.applyAnimalImage(ivSelectedTypeImage, this, selectedAnimalType, overrideImage);
            updatePoultryFieldsVisibility();
        }
    }

    private void updatePoultryFieldsVisibility() {
        boolean isPoultry = selectedAnimalType != null
                && AnimalCatalog.CATEGORY_POULTRY.equals(AnimalCatalog.categorize(selectedAnimalType));
        llPoultryFields.setVisibility(isPoultry ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    dateAcquiredIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    btnDateAcquired.setText(DateUtils.toDisplayFormat(dateAcquiredIso));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveAnimal() {
        if (TextUtils.isEmpty(selectedAnimalType)) {
            Toast.makeText(this, "Please select an animal type", Toast.LENGTH_SHORT).show();
            return;
        }

        String variety = etVariety.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String tagNumber = etTagNumber.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(variety)) {
            etVariety.setError("Variety is required (enter 'Local' or 'Unknown' if unsure)");
            etVariety.requestFocus();
            return;
        }
        if (!ValidationUtils.containsLetter(variety)) {
            etVariety.setError("Variety must include letters, not just numbers");
            etVariety.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(quantity)) {
            etQuantity.setError("Please enter the total number of animals");
            etQuantity.requestFocus();
            return;
        }
        if (!quantity.matches("\\d+") || Integer.parseInt(quantity) < 1) {
            etQuantity.setError("Enter a whole number of 1 or more");
            etQuantity.requestFocus();
            return;
        }

        String layingCount = etLayingCount.getText().toString().trim();
        String traysCollected = etTraysCollected.getText().toString().trim();
        if (llPoultryFields.getVisibility() == View.VISIBLE) {
            if (!TextUtils.isEmpty(layingCount) && !layingCount.matches("\\d+")) {
                etLayingCount.setError("Enter a whole number");
                etLayingCount.requestFocus();
                return;
            }
            if (!TextUtils.isEmpty(traysCollected) && !traysCollected.matches("\\d+")) {
                etTraysCollected.setError("Enter a whole number");
                etTraysCollected.requestFocus();
                return;
            }
        } else {
            layingCount = "";
            traysCollected = "";
        }

        if (TextUtils.isEmpty(tagNumber)) {
            etTagNumber.setError("Tag number is required");
            etTagNumber.requestFocus();
            return;
        }
        if (!tagNumber.matches("\\d+")) {
            etTagNumber.setError("Tag number must contain digits only");
            etTagNumber.requestFocus();
            return;
        }
        if (dateAcquiredIso == null) {
            Toast.makeText(this, "Please select a date acquired", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }
        if (!ValidationUtils.containsLetter(age)) {
            etAge.setError("Please include a unit, e.g. 2 years");
            etAge.requestFocus();
            return;
        }

        Animal animal = new Animal();
        animal.setTagNumber(tagNumber);
        animal.setAnimalType(selectedAnimalType);
        animal.setVariety(variety);
        animal.setQuantity(quantity);
        animal.setDateAcquired(dateAcquiredIso);
        animal.setSex(spinnerSex.getSelectedItem().toString());
        animal.setAge(age);
        animal.setLayingCount(layingCount);
        animal.setTraysCollected(traysCollected);

        long id = dbHelper.addAnimal(animal);
        if (id > 0) {
            Toast.makeText(this, "Animal saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save animal. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}