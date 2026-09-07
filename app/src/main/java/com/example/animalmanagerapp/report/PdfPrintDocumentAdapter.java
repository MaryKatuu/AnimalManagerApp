package com.example.animalmanagerapp.report;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Feeds an already-generated PDF file straight into Android's print
 * pipeline (print preview, printer selection, PDF-to-file, etc.).
 */
public class PdfPrintDocumentAdapter extends PrintDocumentAdapter {

    private final File pdfFile;
    private final String documentName;

    public PdfPrintDocumentAdapter(File pdfFile, String documentName) {
        this.pdfFile = pdfFile;
        this.documentName = documentName;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }
        PrintDocumentInfo info = new PrintDocumentInfo.Builder(documentName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {
        try (FileInputStream in = new FileInputStream(pdfFile);
             FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    return;
                }
                out.write(buffer, 0, bytesRead);
            }
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
        }
    }
}