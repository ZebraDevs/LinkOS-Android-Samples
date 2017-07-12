package com.zebra.pdfprint;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class PDFPick extends DialogFragment {
    private static final String TAG = "PDF_PICK_DIALOG";

    protected MainActivity mainActivity;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");
        mainActivity = (MainActivity) getActivity();
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri fileUri = data.getData();

            String fileName = getPDFName(fileUri);
            Log.i(TAG,"File Name: "+fileName);

            try {
                String filePath = getPDFPath(mainActivity, fileUri);
                Log.i(TAG, "File Path: " + filePath);

                if (filePath != null) {
                    mainActivity.updatePDFInfoTable(fileName, filePath);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.i(TAG,"Bad file");

                String snackbarmsg = mainActivity.getString(R.string.file_access_error);
                mainActivity.showSnackbar(snackbarmsg);
            }


            try {
                int pageWidth = getPageWidth(mainActivity,fileUri);
                mainActivity.fileWidth = pageWidth;
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
                cursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
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
                fileUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
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
            String snackbarmsg = mainActivity.getString(R.string.wrong_firmware);
            mainActivity.showSnackbar(snackbarmsg);
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

}
