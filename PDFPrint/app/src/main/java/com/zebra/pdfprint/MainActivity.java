package com.zebra.pdfprint;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_PRINTER_CONNECT_TAG = "printer_connect";
    private static final String DIALOG_PDF_PICK_TAG = "pdf_pick";
    private static final String DIALOG_PRINT_TAG = "print";

    protected TextView printerSelectionStatus;
    protected TextView printerInfoTableName;
    protected TextView printerInfoTableAddress;
    protected TableLayout printerInfoTable;

    protected TextView pdfSelectionStatus;
    protected TextView pdfInfoTableName;
    protected TextView pdfInfoTablePath;
    protected TableLayout pdfInfoTable;

    protected TextView printButton;

    protected DiscoveredPrinter chosenPrinter;
    protected String MacAddress;
    protected String filePath = null;
    protected Integer fileWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printerSelectionStatus = (TextView) findViewById(R.id.printerSelectionStatus);
        printerInfoTableName = (TextView) findViewById(R.id.printerInfoTableName);
        printerInfoTableAddress = (TextView) findViewById(R.id.printerInfoTableAddress);
        printerInfoTable = (TableLayout) findViewById(R.id.printerInfoTable);

        pdfSelectionStatus = (TextView) findViewById(R.id.pdfSelectionStatus);
        pdfInfoTableName = (TextView) findViewById(R.id.pdfInfoTableName);
        pdfInfoTablePath = (TextView) findViewById(R.id.pdfInfoTablePath);
        pdfInfoTable = (TableLayout) findViewById(R.id.pdfInfoTable);


        TextView selectPrinterButton = (TextView) findViewById(R.id.selectPrinterButton);
        selectPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PRINTER_CONNECT_TAG,"Select Printer button clicked");

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                com.zebra.pdfprint.PrinterConnectionDialog printerConnectionDialog = (com.zebra.pdfprint.PrinterConnectionDialog) getFragmentManager().findFragmentByTag(DIALOG_PRINTER_CONNECT_TAG);

                if (printerConnectionDialog == null) {
                    printerConnectionDialog = new com.zebra.pdfprint.PrinterConnectionDialog();
                    printerConnectionDialog.show(ft, DIALOG_PRINTER_CONNECT_TAG);
                }
            }
        });

        TextView selectPDFButton = (TextView) findViewById(R.id.selectPDFButton);
        selectPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PDF_PICK_TAG, "Select PDF button clicked");

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                pickPdf();
            }
        });

        printButton = (TextView) findViewById(R.id.printNowButton);
        printButton.setOnClickListener(null);
    }

    private void pickPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri fileUri = data.getData();

            String fileName = getPDFName(fileUri);
            Log.i("PDF Pick Response","File Name: "+fileName);

            try {
                String filePath = getPDFPath(this, fileUri);
                Log.i("PDF Pick Response", "File Path: " + filePath);

                if (filePath != null) {
                    updatePDFInfoTable(fileName, filePath);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.i("PDF Pick Response","Bad file");

                String snackbarmsg = getString(R.string.file_access_error);
                showSnackbar(snackbarmsg);
            }


            try {
                int pageWidth = getPageWidth(this, fileUri);
                fileWidth = pageWidth;
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    // Uses the Uri to obtain the name of the pdf.
    public String getPDFName(Uri fileUri) {
        String fileString = fileUri.toString();
        File myFile = new File(fileString);
        String fileName = null;

        if (fileString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(fileUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (fileString.startsWith("file://")) {
            fileName = myFile.getName();
        }
        return fileName;
    }

    // Uses the Uri to obtain the path to the file.
    public String getPDFPath(Context context, Uri fileUri) {
        String selection = null;
        String[] selectionArgs = null;

        final String id = DocumentsContract.getDocumentId(fileUri);
        try {
            if (id.length() < 15) {
                fileUri = ContentUris
                    .withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (id.substring(0,7).equals("primary")) {
                String endPath = id.substring(8);
                String fullPath = "/sdcard/" + endPath;
                return fullPath;
            } else if (!id.substring(0,1).equals("/")) {
                boolean pathStarted = false;
                String path = "/sdcard/";

                for (char c : id.toCharArray()) {
                    if (pathStarted) {
                        path = path + c;
                    }
                    if (c == ':') {
                        pathStarted = true;
                    }
                }
                return path;
            } else {
                return id;
            }
        } catch (NumberFormatException e) {
            String snackbarmsg = getString(R.string.wrong_firmware);
            showSnackbar(snackbarmsg);
        }

        if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            String[] projection = {
                MediaStore.Files.FileColumns.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                    .query(fileUri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    // Returns the width of the page in inches for scaling later
    // PdfRenderer is only available for devices running Android Lollipop or newer
    private Integer getPageWidth(Context context, Uri fileUri) throws IOException {
        final ParcelFileDescriptor pfdPdf = context.getContentResolver().openFileDescriptor(
            fileUri, "r");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PdfRenderer pdf = new PdfRenderer(pfdPdf);
            PdfRenderer.Page page = pdf.openPage(0);
            int pixWidth = page.getWidth();
            int inWidth = pixWidth / 72;
            return inWidth;
        }

        return null;
    }

    public String MacAddress(){
        return MacAddress;
    }

    public String FilePath(){
        return filePath;
    }

    public Integer FileWidth(){
        return fileWidth;
    }

    public void displayConnectingStatus() {
        printerSelectionStatus.setText(getString(R.string.connecting_to_printer));
    }

    public void updatePrinterInfoTable(DiscoveredPrinter discoveredPrinter) {
        printerInfoTableName.setText(discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
        printerInfoTableAddress.setText(discoveredPrinter.getDiscoveryDataMap().get("HARDWARE_ADDRESS"));
        printerInfoTable.setVisibility(View.VISIBLE);
        printerSelectionStatus.setText(getString(R.string.connected_to_printer));
        chosenPrinter=discoveredPrinter;

        MacAddress=discoveredPrinter.toString();
        updatePrintButton();
    }

    public void updatePDFInfoTable(String pdfName, String pdfPath) {
        pdfInfoTableName.setText(pdfName);
        pdfInfoTablePath.setText(pdfPath);
        pdfInfoTable.setVisibility(View.VISIBLE);
        pdfSelectionStatus.setText(getString(R.string.selected_a_pdf));

        filePath = pdfPath;
        updatePrintButton();
    }

    public void resetConnectingStatus() {
        printerInfoTable.setVisibility(View.GONE);
        printerSelectionStatus.setText(getString(R.string.no_printer_selected));
    }

    public void showSnackbar(String snackbarText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.near_black));
        TextView snackbarTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void updatePrintButton() {
        if ((filePath != null) && (MacAddress != null)) {
            printButtonEnable();
        }else {
            printButtonDisable();
        }
    }

    public void printButtonEnable() {
        printButton = (TextView) findViewById(R.id.printNowButton);

        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.zebra_red));
        printButton.setText(R.string.print);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PrintDialog printDialog = new com.zebra.pdfprint.PrintDialog();
                printDialog.show(ft, DIALOG_PRINT_TAG);
            }
        });
    }

    public void printButtonDisable() {
        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        printButton.setText(R.string.unable_to_print);
        printButton.setOnClickListener(null);
    }
}
