package com.zebra.pdfprint;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

public class PrintDialog extends DialogFragment {
    private static final String TAG = "SIGNTR_PRINT_DIALOG";
    private MainActivity mainActivity;
    private String filePath;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");

        mainActivity = (MainActivity) getActivity();
        filePath = mainActivity.FilePath();

        new SendPDF((MainActivity) getActivity(), filePath).execute();
        return null;
    }
}
