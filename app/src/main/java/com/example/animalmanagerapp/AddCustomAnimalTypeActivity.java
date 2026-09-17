package com.example.animalmanagerapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.catalog.ImageStorageUtils;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.ValidationUtils;

/**
 * Lets a farmer add a custom animal type not in the built-in catalog,
 * with an optional real photo picked from the gallery. Custom types are
 * stored via the crop-style "unmarked" storage — here, simply as a
 * hidden-types opt-out list entry plus an image override, keyed by name;
 * the type itself becomes available the moment it's typed into a form.
 */
public class AddCustomAnimalTypeActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PHOTO = 400;

    private ImageView ivPhotoPreview;
    private EditText etTypeName;
    private DatabaseHelper dbHelper;
    private String pickedImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_animal_type);

        dbHelper = new DatabaseHelper(this);

        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        etTypeName = findViewById(R.id.etTypeName);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSaveCustomType = findViewById(R.id.btnSaveCustomType);

        findViewById(R.id.framePhoto).setOnClickListener(v -> pickPhoto());

        btnCancel.setOnClickListener(v -> finish());
        btnSaveCustomType.setOnClickListener(v -> saveCustomType());
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                String savedPath = ImageStorageUtils.copyToInternalStorage(this, selectedUri);
                if (savedPath != null) {
                    pickedImagePath = savedPath;
                    ivPhotoPreview.setPadding(0, 0, 0, 0);
                    ivPhotoPreview.setImageURI(Uri.fromFile(new java.io.File(savedPath)));
                } else {
                    Toast.makeText(this, "Could not load that photo. Please try another.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveCustomType() {
        String name = etTypeName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etTypeName.setError("Please enter an animal type");
            etTypeName.requestFocus();
            return;
        }
        if (!ValidationUtils.containsLetter(name)) {
            etTypeName.setError("Animal type must include letters, not just numbers");
            etTypeName.requestFocus();
            return;
        }

        if (pickedImagePath != null) {
            dbHelper.setAnimalTypeImage(name, pickedImagePath);
        }

        Intent result = new Intent();
        result.putExtra("type_name", name);
        setResult(RESULT_OK, result);
        finish();
    }
}