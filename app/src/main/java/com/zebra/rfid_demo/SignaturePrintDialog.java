package com.zebra.rfid_demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SignaturePrintDialog extends DialogFragment {
    private static final String TAG = "SIGNTR_PRINT_DIALOG";

    private SignatureArea signatureArea;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");

        View view = View.inflate(getActivity(), R.layout.signature_print_dialog, null);
        signatureArea = (SignatureArea) view.findViewById(R.id.signatureArea);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.print), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap signatureBitmap = Bitmap.createScaledBitmap(signatureArea.getBitmap(), 200, 70, false);

                new PrintTask((MainActivity) getActivity(), signatureBitmap).execute();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
