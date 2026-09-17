package com.example.animalmanagerapp.catalog;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/** Copies a picked gallery image into the app's own internal storage. */
public class ImageStorageUtils {

    private static final String FOLDER_NAME = "animal_images";

    public static String copyToInternalStorage(Context context, Uri sourceUri) {
        try {
            File folder = new File(context.getFilesDir(), FOLDER_NAME);
            if (!folder.exists()) folder.mkdirs();

            File destFile = new File(folder, "animal_" + UUID.randomUUID() + ".jpg");

            try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }
}